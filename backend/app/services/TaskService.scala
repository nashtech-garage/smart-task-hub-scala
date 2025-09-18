package services

import dto.request.task.{CreateTaskRequest, UpdateTaskRequest}
import dto.response.task.{TaskDetailResponse, TaskSummaryResponse}
import dto.websocket.OutgoingMessage
import dto.websocket.board.{TaskCreated, TaskStatusUpdated, TaskUpdated}
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
class TaskService @Inject()(taskRepository: TaskRepository,
                            columnRepository: ColumnRepository,
                            protected val dbConfigProvider: DatabaseConfigProvider,
                            broadcastService: BroadcastService,
                            projectRepository: ProjectRepository
                           )(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

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
          position = Some(request.position)
        )
        taskRepository.create(newTask)
      }
    } yield (taskId, projectId)

    val result = db.run(action.transactionally)
    result.onComplete(res => {
      res.map {
        case (taskId, projectId) =>
          val message = TaskCreated(
            taskId = taskId,
            columnId = columnId,
            name = request.name,
            taskPosition = request.position
          )
          broadcastService.broadcastToProject(projectId, message)
      }
    })
    result.map(_._1)
  }

  def updateTask(taskId: Int, req: UpdateTaskRequest, updatedBy: Int): Future[Task] = {
    val action = for {
      (task, projectId) <- getTaskAndProjectByTaskId(taskId, updatedBy)

      _ <- if (task.status == models.Enums.TaskStatus.active) {
        DBIO.successful(())
      } else {
        DBIO.failed(AppException(
          message = s"Task with ID $taskId does not exist or is not active",
          statusCode = Status.NOT_FOUND))
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
        val message = TaskUpdated(
          taskId = task.id.get,
          columnId = task.columnId,
          taskPosition = task.position.getOrElse(0),
          detail = TaskMapper.toDetailResponse(task)
        )
        broadcastService.broadcastToProject(project, message)
      case _ => Logger("application").error("Failed to broadcast task update")
    }
    result.map { case (task, _) => task }
  }

  def getTaskDetailById(taskId: Int, userId: Int): Future[Option[TaskDetailResponse]] = {
    val action = for {
      result <- getTaskAndProjectByTaskId(taskId, userId)
    } yield result

    db.run(action.transactionally).map {
      case (task, _) => Some(TaskMapper.toDetailResponse(task))
    }
  }

  def archiveTask(taskId: Int, userId: Int): Future[Task] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.active),
      next = TaskStatus.archived,
      errorMsg = "Only active tasks can be archived",
      broadcastMessage = TaskStatusUpdated(
        taskId = taskId,
        columnId = 0, // Placeholder, actual columnId should be fetched if needed
        taskPosition = 0, // Placeholder, actual position should be fetched if needed
        name = "",
        updatedStatus = TaskStatus.archived
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
        taskId = taskId,
        columnId = 0, // Placeholder, actual columnId should be fetched if needed
        taskPosition = 0, // Placeholder, actual position should be fetched if needed
        name = "",
        updatedStatus = TaskStatus.active
      )
    )
  }

  def deleteTask(taskId: Int, userId: Int): Future[Task] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.archived),
      next = TaskStatus.deleted,
      errorMsg = "Only archived tasks can be deleted",
      broadcastMessage = TaskStatusUpdated(
        taskId = taskId,
        columnId = 0, // Placeholder, actual columnId should be fetched if needed
        taskPosition = 0, // Placeholder, actual position should be fetched if needed
        name = "", // Placeholder, actual name should be fetched if needed
        updatedStatus = TaskStatus.deleted
      )
    )
  }

  def getArchivedTask(projectId: Int, userId: Int): Future[Seq[TaskSummaryResponse]] = {
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

  private def changeStatus(taskId: Int,
                           userId: Int,
                           validFrom: Set[TaskStatus],
                           next: TaskStatus,
                           errorMsg: String,
                           broadcastMessage: OutgoingMessage
                          ): Future[Task] = {
    val action = for {
      (task, projectId) <- getTaskAndProjectByTaskId(taskId, userId)
      _ <- if (validFrom.contains(task.status)) {
        taskRepository.update(task.copy(
          status = next,
          updatedBy = Some(userId),
          updatedAt = Instant.now()
        ))
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
              columnId = task.columnId,
              taskPosition = task.position.getOrElse(0),
              name = task.name
            )
          case other => other
        }
        broadcastService.broadcastToProject(project, message)
      case _ => Logger("application").error("Failed to broadcast task status change")
    }
    result.map { case (task, _) => task }
  }

  private def getTaskAndProjectByTaskId(taskId: Int, userId: Int): DBIO[(Task, Int)] = {
    for {
      taskOpt <- taskRepository.findTaskAndProjectIdIfUserInProject(taskId, userId)
      result <- taskOpt match {
        case Some((t, pi)) => DBIO.successful(t, pi)
        case _ => DBIO.failed(AppException(
          message = s"Task with ID $taskId does not exist",
          statusCode = Status.NOT_FOUND)
        )
      }
    } yield result
  }

}
