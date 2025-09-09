package services

import dto.request.task.{CreateTaskRequest, UpdateTaskRequest}
import dto.response.task.TaskDetailResponse
import exception.AppException
import mappers.TaskMapper
import models.Enums.TaskStatus
import models.Enums.TaskStatus.TaskStatus
import models.entities.Task
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{ColumnRepository, TaskRepository}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskService @Inject()(taskRepository: TaskRepository,
                            columnRepository: ColumnRepository,
                            protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]  {

  import profile.api._

  def createNewTask(request: CreateTaskRequest, columnId: Int, createdBy: Int): Future[Int] = {
    val action = for {
      columnOpt <- columnRepository.findColumnIfUserInProject(columnId, createdBy)
      _ <- columnOpt match {
        case Some(_) => DBIO.successful()
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
    } yield taskId

    db.run(action.transactionally)
  }

  def updateTask(taskId: Int, req: UpdateTaskRequest, updatedBy: Int): Future[Int] = {
    val action = for {
      taskOpt <- getTaskById(taskId, updatedBy)
      task <- taskOpt match {
        case Some(t) if t.status == models.Enums.TaskStatus.active => DBIO.successful(t)
        case _ => DBIO.failed(AppException(
          message = s"Task with ID $taskId does not exist or is not active",
          statusCode = Status.NOT_FOUND)
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

      rowsAffected <- taskRepository.update(updatedTask)

    } yield rowsAffected

    db.run(action.transactionally)
  }

  def getTaskDetailById(taskId: Int, userId: Int): Future[Option[TaskDetailResponse]] = {
    val action = for {
      task <- getTaskById(taskId, userId)
    } yield task

    db.run(action.transactionally).map(_.map(TaskMapper.toDetailResponse))
  }

  def archiveTask(taskId: Int, userId: Int): Future[Int] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.active),
      next = TaskStatus.archived,
      errorMsg = "Only active tasks can be archived"
    )
  }

  def restoreTask(taskId: Int, userId: Int): Future[Int] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.archived),
      next = TaskStatus.active,
      errorMsg = "Only archived tasks can be restored"
    )
  }

  def deleteTask(taskId: Int, userId: Int): Future[Int] = {
    changeStatus(
      taskId = taskId,
      userId = userId,
      validFrom = Set(TaskStatus.archived),
      next = TaskStatus.deleted,
      errorMsg = "Only archived tasks can be deleted"
    )
  }

  private def changeStatus(taskId: Int,
                           userId: Int,
                           validFrom: Set[TaskStatus],
                           next: TaskStatus,
                           errorMsg: String): Future[Int] = {
    val action = for {
      task <- getTaskById(taskId, userId)
      maybeStatus = task.map(_.status)
      updatedRows <- maybeStatus match {
        case Some(s) if validFrom.contains(s) =>
          taskRepository.update(task.get.copy(
            status = next,
            updatedBy = Some(userId),
            updatedAt = Instant.now()
          ))
        case Some(_) =>
          DBIO.failed(AppException(errorMsg, Status.BAD_REQUEST))
        case None =>
          DBIO.failed(AppException(
            message = s"Task with ID $taskId does not exist",
            statusCode = Status.NOT_FOUND)
          )
      }
    } yield updatedRows

    db.run(action)
  }

  private def getTaskById(taskId: Int, userId: Int): DBIO[Option[Task]] = {
    for {
      taskOpt <- taskRepository.findTaskIfUserInProject(taskId, userId)
      task <- taskOpt match {
        case Some(t) => DBIO.successful(Some(t))
        case _ => DBIO.failed(AppException(
          message = s"Task with ID $taskId does not exist",
          statusCode = Status.NOT_FOUND)
        )
      }
    } yield task
  }

}
