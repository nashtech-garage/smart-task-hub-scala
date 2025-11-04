package services

import cache.TaskCacheManager
import dto.request.task.{
  CreateTaskRequest,
  UpdateTaskPositionRequest,
  UpdateTaskRequest
}
import dto.response.task.{
  TaskDetailResponse,
  TaskSearchResponse,
  TaskSummaryResponse
}
import dto.websocket.OutgoingMessage
import dto.websocket.board.messageTypes._
import exception.AppException
import mappers.TaskMapper
import models.Enums.TaskStatus
import models.Enums.TaskStatus.TaskStatus
import models.entities.Task
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{ColumnRepository, ProjectRepository, TaskRepository}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class TaskService @Inject()(
  taskRepository: TaskRepository,
  columnRepository: ColumnRepository,
  protected val dbConfigProvider: DatabaseConfigProvider,
  broadcastService: BroadcastService,
  projectRepository: ProjectRepository,
  taskCacheManager: TaskCacheManager
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def createNewTask(request: CreateTaskRequest, columnId: Int, createdBy: Int): Future[Int] = {
    val action = for {
      columnOpt <- columnRepository.findColumnIfUserInProject(columnId, createdBy)
      projectId <- columnOpt match {
        case Some(_) => DBIO.successful(columnOpt.get.projectId)
        case _ => DBIO.failed(AppException(
          message = s"Column with ID $columnId does not exist or is not active",
          statusCode = Status.NOT_FOUND)
        )
      }

      existByPosition <- taskRepository.existsByPositionAndActiveTrueInColumn(request.position, columnId)
      _ <- if (existByPosition) {
        DBIO.failed(AppException(
          message = "Task position already exists in the column",
          statusCode = Status.CONFLICT)
        )
      } else {
        DBIO.successful(())
      }

      taskId <- {
        val newTask = Task(
          name = request.name,
          columnId = columnId,
          createdBy = Some(createdBy),
          updatedBy = Some(createdBy),
          position = request.position
        )
        taskRepository.create(newTask)
      }
    } yield (taskId, projectId)

    val result = db.run(action.transactionally)
    result.onComplete(res => {
      res.map {
        case (taskId, projectId) =>
          val message = TaskCreated(
            TaskCreatedPayload(
              taskId = taskId,
              columnId = columnId,
              name = request.name,
              taskPosition = request.position
            )
          )
          broadcastService.broadcastToProject(projectId, message)
      }
    })
    result.map(_._1)
  }

  def updateTask(taskId: Int,
                 req: UpdateTaskRequest,
                 updatedBy: Int): Future[Task] = {
    val action = for {
      (task, projectId) <- getTaskAndProjectByTaskId(taskId, updatedBy)

      _ <- if (task.status == models.Enums.TaskStatus.active) {
        DBIO.successful(())
      } else {
        DBIO.failed(
          AppException(
            message = s"Task with ID $taskId does not exist or is not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }

      updatedTask = task.copy(
        name = req.name,
        description = req.description,
        startDate = req.startDate,
        endDate = req.endDate,
        priority = req.priority,
        updatedBy = Some(updatedBy),
        updatedAt = Instant.now(),
        isCompleted = req.isCompleted.getOrElse(false)
      )

      _ <- taskRepository.update(updatedTask)

    } yield (updatedTask, projectId)

    val result = db.run(action.transactionally)
    result.onComplete {
      case Success((task, project)) =>
        // Invalidate cache for the column since the task is updated
        taskCacheManager.updateTaskFields(task)
        val message = TaskUpdated(
          TaskUpdatedPayload(
            taskId = task.id.get,
            columnId = task.columnId,
            taskPosition = task.position,
            detail = TaskMapper.toDetailResponse(task)
          )
        )
        broadcastService.broadcastToProject(project, message)
      case _ => Logger("application").error("Failed to broadcast task update")
    }
    result.map { case (task, _) => task }
  }

  def getTaskDetailById(taskId: Int,
                        userId: Int): Future[Option[TaskDetailResponse]] = {
    val action = for {
      (task, _) <- getTaskAndProjectByTaskId(taskId, userId)
      assignedMembers <- taskRepository.findAssignedMembers(taskId)
    } yield (task, assignedMembers)

    db.run(action.transactionally).map {
      case (task, assignedMembers) =>
        Some(
          TaskMapper.toDetailWithAssignMembersResponse(task, assignedMembers)
        )
    }
  }

  private def getTaskAndProjectByTaskId(taskId: Int,
                                        userId: Int): DBIO[(Task, Int)] = {
    for {
      taskOpt <- taskRepository.findTaskAndProjectIdIfUserInProject(
        taskId,
        userId
      )
      result <- taskOpt match {
        case Some((t, pi)) => DBIO.successful(t, pi)
        case _ =>
          DBIO.failed(
            AppException(
              message = s"Task with ID $taskId does not exist",
              statusCode = Status.NOT_FOUND
            )
          )
      }
    } yield result
  }

  def archiveTask(taskId: Int, userId: Int): Future[Task] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.active),
      next = TaskStatus.archived,
      errorMsg = "Only active tasks can be archived",
      broadcastMessage = TaskStatusUpdated(
        TaskStatusUpdatedPayload(
          taskId = taskId,
          columnId = 0, // Placeholder, actual columnId should be fetched if needed
          taskPosition = 0, // Placeholder, actual position should be fetched if needed
          name = "",
          updatedStatus = TaskStatus.archived
        )
      )
    )
  }

  def restoreTask(taskId: Int, userId: Int): Future[Task] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.archived),
      next = TaskStatus.active,
      errorMsg = "Only archived tasks can be restored",
      broadcastMessage = TaskStatusUpdated(
        TaskStatusUpdatedPayload(
          taskId = taskId,
          columnId = 0, // Placeholder, actual columnId should be fetched if needed
          taskPosition = 0, // Placeholder, actual position should be fetched if needed
          name = "",
          updatedStatus = TaskStatus.active
        )
      )
    )
  }

  private def changeStatus(taskId: Int,
                           userId: Int,
                           validFrom: Set[TaskStatus],
                           next: TaskStatus,
                           errorMsg: String,
                           broadcastMessage: OutgoingMessage): Future[Task] = {
    val action = for {
      (task, projectId) <- getTaskAndProjectByTaskId(taskId, userId)
      _ <- if (validFrom.contains(task.status)) {
        taskRepository.update(
          task.copy(
            status = next,
            updatedBy = Some(userId),
            updatedAt = Instant.now()
          )
        )
      } else {
        DBIO.failed(AppException(errorMsg, Status.BAD_REQUEST))
      }
    } yield (task, projectId)

    val result = db.run(action)
    result.onComplete {
      case Success((task, project)) =>
        val message = broadcastMessage match {
          case msg: TaskStatusUpdated =>
            msg.copy(
              payload = msg.payload.copy(
                columnId = task.columnId,
                taskPosition = task.position,
                name = task.name
              )
            )
          case other => other
        }
        broadcastService.broadcastToProject(project, message)
      case _ =>
        Logger("application").error("Failed to broadcast task status change")
    }
    result.map { case (task, _) => task }
  }

  def deleteTask(taskId: Int, userId: Int): Future[Task] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.archived),
      next = TaskStatus.deleted,
      errorMsg = "Only archived tasks can be deleted",
      broadcastMessage = TaskStatusUpdated(
        TaskStatusUpdatedPayload(
          taskId = taskId,
          columnId = 0, // Placeholder, actual columnId should be fetched if needed
          taskPosition = 0, // Placeholder, actual position should be fetched if needed
          name = "", // Placeholder, actual name should be fetched if needed
          updatedStatus = TaskStatus.deleted
        )
      )
    )
  }

  def getArchivedTask(projectId: Int,
                      userId: Int): Future[Seq[TaskSummaryResponse]] = {
    val action = for {
      isUserInActiveProject <- projectRepository.isUserInActiveProject(
        userId,
        projectId
      )
      result <- if (isUserInActiveProject) {
        taskRepository.findArchivedTasksByProjectId(projectId)
      } else {
        DBIO.failed(
          AppException(
            message = s"Project not found",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(action.transactionally).map { tasks =>
      tasks
    }
  }

  def assignMemberToTask(projectId: Int,
                         taskId: Int,
                         userId: Int,
                         assignedBy: Int): Future[Int] = {
    val action = for {
      (task, projectId) <- getTaskAndProjectByTaskId(taskId, assignedBy)

      _ <- if (task.status == models.Enums.TaskStatus.active) {
        DBIO.successful(())
      } else {
        DBIO.failed(
          AppException(
            message = s"Task with ID $taskId does not exist or is not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }

      userCheckOpt <- taskRepository.findUserInProjectNotAssigned(
        userId,
        taskId
      )
      _ <- userCheckOpt match {
        case Some(_) => {
          val assignedMemberToTaskMessage = MemberAssignedToTask(
            MemberAssignedToTaskPayload(taskId, userCheckOpt.get)
          )
          broadcastService.broadcastToProject(
            projectId,
            assignedMemberToTaskMessage
          )
          DBIO.successful(())
        }
        case None =>
          DBIO.failed(
            AppException(
              message =
                s"User with ID $userId is not in the project or already assigned",
              statusCode = Status.NOT_FOUND
            )
          )
      }

      rowsAffected <- taskRepository.assignMemberToTask(
        taskId,
        userId,
        Some(assignedBy)
      )
    } yield rowsAffected

    db.run(action)
  }

  def unassignMemberFromTask(projectId: Int,
                             taskId: Int,
                             userId: Int,
                             unassignedBy: Int): Future[Int] = {
    val action = for {
      isUserInProject <- projectRepository.isUserInActiveProject(
        unassignedBy,
        projectId
      )

      _ <- if (isUserInProject) {
        DBIO.successful(())
      } else {
        DBIO.failed(
          AppException(
            message =
              s"You do not have permission to unassign members from this task",
            statusCode = Status.FORBIDDEN
          )
        )
      }

      deletionCheck <- taskRepository.unassignMemberFromTask(taskId, userId)
      _ <- deletionCheck match {
        case 1 =>
          val unassignedMemberFromTaskMessage = MemberUnassignedFromTask(
            MemberUnassignedFromTaskPayload(taskId, userId)
          )
          broadcastService.broadcastToProject(
            projectId,
            unassignedMemberFromTaskMessage
          )
          DBIO.successful(())

        case 0 =>
          DBIO.failed(
            AppException(
              message =
                s"User with ID $userId is not in the project or not assigned to the task",
              statusCode = Status.BAD_REQUEST
            )
          )
      }
    } yield deletionCheck

    db.run(action)
  }

  def getActiveTasksInProject(projectId: Int,
                              userId: Int): Future[Seq[TaskSummaryResponse]] = {
    val action = for {
      isUserInActiveProject <- projectRepository.isUserInActiveProject(
        userId,
        projectId
      )
      result <- if (isUserInActiveProject) {
        taskRepository.findActiveTaskByProjectId(projectId)
      } else {
        DBIO.failed(
          AppException(
            message = s"Project not found",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(action.transactionally).map { tasks =>
      tasks
    }
  }

  def getActiveTasksInColumn(projectId: Int,
                             columnId: Int,
                             userId: Int,
                             limit: Int,
                             offset: Int): Future[Seq[TaskSummaryResponse]] = {

    taskCacheManager.getTasks(columnId, offset, limit).flatMap {
      case Some(cachedTasks) =>
        Future.successful(cachedTasks)

      case None =>
        val action = for {
          isUserInActiveProject <- projectRepository
            .isUserInActiveProject(userId, projectId)
          tasks <- if (isUserInActiveProject) {
            taskRepository.findActiveTaskByColumnId(columnId, limit, offset)
          } else {
            DBIO.failed(
              AppException(
                message = "Project not found",
                statusCode = Status.NOT_FOUND
              )
            )
          }
        } yield tasks

        db.run(action).flatMap { tasks =>
          taskCacheManager.addTasksToColumn(columnId, tasks).map(_ => tasks)
        }
    }
  }

  def searchTasks(projectIds: Option[Seq[Int]],
                  keyword: Option[String],
                  page: Int,
                  size: Int,
                  userId: Int): Future[Seq[TaskSearchResponse]] = {

    val query = taskRepository
      .search(projectIds, keyword, userId)
      .drop((page - 1) * size)
      .take(size)

    db.run(query.result)
      .map(_.map {
        case (
            taskId,
            taskName,
            taskDesc,
            taskStatus,
            projectId,
            projectName,
            columnName,
            updatedAt
            ) =>
          TaskSearchResponse(
            taskId,
            taskName,
            taskDesc,
            taskStatus,
            projectId,
            projectName,
            columnName,
            updatedAt
          )
      })
  }

  def updatePosition(taskId: Int, req: UpdateTaskPositionRequest, userId: Int): Future[Int] = {
    val action = for {
      (task, projectId) <- getTaskAndProjectByTaskId(taskId, userId)

      _ <- if (task.status == TaskStatus.active) {
        DBIO.successful(())
      } else {
        DBIO.failed(AppException(
          message = s"Task with ID $taskId is not active",
          statusCode = Status.NOT_FOUND))
      }

      updatedRows <- taskRepository.updatePosition(taskId, req.position, req.columnId)
      _ <- updatedRows match {
        case 1 =>
          broadcastService.broadcastToProject(
            projectId,
            TaskMoved(TaskMovedPayload(taskId, task.columnId, req.columnId, req.position)))
          DBIO.successful(())
        case 0 =>
          DBIO.failed(AppException(
            message = s"Failed to update task position",
            statusCode = Status.BAD_REQUEST
          ))
      }
    } yield (task, projectId)
    db.run(action.transactionally).flatMap { case (task, projectId) =>
      taskCacheManager.updateTaskPosition(
        newColumnId = req.columnId,
        taskId = taskId,
        newPosition = req.position
      ).map(_ => 1)
    }
  }
}
