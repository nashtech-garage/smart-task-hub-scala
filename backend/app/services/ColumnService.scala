package services

import dto.request.column.{CreateColumnRequest, UpdateColumnPositionRequest, UpdateColumnRequest}
import dto.response.column.{ColumnSummariesResponse, ColumnWithTasksResponse}
import dto.websocket.board.messageTypes.{ColumnMoved, ColumnMovedPayload}
import exception.AppException
import models.Enums.ColumnStatus
import models.Enums.ColumnStatus.ColumnStatus
import models.entities.Column
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{ColumnRepository, ProjectRepository}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ColumnService @Inject()(
  columnRepository: ColumnRepository,
  projectRepository: ProjectRepository,
  broadcastService: BroadcastService,
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def createColumn(req: CreateColumnRequest,
                   projectId: Int,
                   userId: Int): Future[Int] = {
    val checkAndInsert = for {
      exists <- projectRepository.isUserInActiveProject(userId, projectId)
      exitsByPosition <- columnRepository.exitsByPosition(
        projectId,
        req.position
      )
      _ <- if (exitsByPosition) {
        DBIO.failed(
          AppException(
            message =
              s"Column position ${req.position} already exists in project $projectId",
            statusCode = Status.CONFLICT
          )
        )
      } else {
        DBIO.successful(())
      }
      result <- if (exists) {
        val newColumn = Column(
          projectId = projectId,
          name = req.name,
          position = req.position
        )
        columnRepository.create(newColumn)
      } else {
        DBIO.failed(
          AppException(
            message = s"Project $projectId is not found or not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(checkAndInsert)
  }

  def getActiveColumnsWithTasks(
    projectId: Int,
    userId: Int
  ): Future[Seq[ColumnWithTasksResponse]] = {
    val checkAndGet = for {
      exists <- projectRepository.isUserInActiveProject(userId, projectId)
      result <- if (exists) {
        columnRepository.findActiveColumnsWithTasks(projectId)
      } else {
        DBIO.failed(
          AppException(
            message = s"Project $projectId is not exists or not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(checkAndGet)
  }

  def updateColumn(req: UpdateColumnRequest,
                   columnId: Int,
                   projectId: Int,
                   userId: Int): Future[Int] = {
    val checkAndUpdate = for {
      isUserInActiveProject <- projectRepository.isUserInActiveProject(
        userId,
        projectId
      )
      result <- if (isUserInActiveProject) {
        columnRepository.update(req, columnId)
      } else {
        DBIO.failed(
          AppException(
            message = s"Column ${columnId} is not found or not active",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(checkAndUpdate)
  }

  private def changeStatus(columnId: Int,
                           userId: Int,
                           validFrom: Set[ColumnStatus],
                           next: ColumnStatus,
                           errorMsg: String): Future[Int] = {
    val action = for {
      maybeStatus <- columnRepository.findStatusIfUserInProject(
        columnId,
        userId
      )
      updatedRows <- maybeStatus match {
        case Some(s) if validFrom.contains(s) =>
          columnRepository.updateStatus(columnId, next)
        case Some(_) =>
          DBIO.failed(AppException(errorMsg, Status.BAD_REQUEST))
        case None =>
          DBIO.failed(AppException("Column not found", Status.NOT_FOUND))
      }
    } yield updatedRows

    db.run(action)
  }

    def archiveColumn(columnId: Int, userId: Int): Future[Int] =
        changeStatus(
        columnId,
        userId,
        validFrom = Set(ColumnStatus.active),
        next = ColumnStatus.archived,
        errorMsg = "Only active columns can be archived"
        )

    def restoreColumn(columnId: Int, userId: Int): Future[Int] =
        changeStatus(
        columnId,
        userId,
        validFrom = Set(ColumnStatus.archived),
        next = ColumnStatus.active,
        errorMsg = "Only archived columns can be restored"
        )

    def deleteColumn(columnId: Int, userId: Int): Future[Int] =
        changeStatus(
        columnId,
        userId,
        validFrom = Set(ColumnStatus.archived),
        next = ColumnStatus.deleted,
        errorMsg = "Only archived columns can be deleted"
        )

  def updatePosition(projectId: Int,
                     columnId: Int,
                     request: UpdateColumnPositionRequest,
                     userId: Int): Future[Int] = {
    val action = for {
      maybeStatus <- columnRepository.findStatusIfUserInProject(
        columnId,
        userId
      )
      updatedRows <- maybeStatus match {
        case Some(s) if s == ColumnStatus.active =>
          columnRepository.updatePosition(columnId, request.position)
        case Some(_) =>
          DBIO.failed(AppException("Only active columns can change position", Status.BAD_REQUEST))
        case None =>
          DBIO.failed(AppException("Column not found", Status.NOT_FOUND))
      }
    } yield updatedRows

    val resultF: Future[Int] = db.run(action)
    resultF.foreach { _ =>
      broadcastService.broadcastToProject(
        projectId,
        ColumnMoved(ColumnMovedPayload(columnId, request.position))
      )
    }
    resultF
  }

  def getArchivedColumns(projectId: Int, userId: Int): Future[Seq[ColumnSummariesResponse]] = {
    val find = for {
      isUserInActiveProject <- projectRepository.isUserInActiveProject(
        userId,
        projectId
      )
      result <- if (isUserInActiveProject) {
        columnRepository.findArchivedColumnsByProjectId(projectId)
      } else {
        DBIO.failed(
          AppException(
            message = s"Project not found",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield result

    db.run(find)
  }
}
