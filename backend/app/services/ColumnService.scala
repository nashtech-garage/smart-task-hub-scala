package services

import dto.request.column.{CreateColumnRequest, UpdateColumnPositionRequest, UpdateColumnRequest}
import dto.response.column.{ColumnSummariesResponse, ColumnWithTasksResponse}
import dto.websocket.OutgoingMessage
import dto.websocket.board.messageTypes.{ColumnCreated, ColumnCreatedPayload, ColumnMoved, ColumnMovedPayload, ColumnStatusUpdated, ColumnStatusUpdatedPayload, ColumnUpdated, ColumnUpdatedPayload}
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
    db.run(checkAndInsert).map {
      columnId =>
        broadcastService.broadcastToProject(projectId, ColumnCreated(ColumnCreatedPayload(columnId, req.position, req.name)))
        columnId
    }
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

    db.run(checkAndUpdate).map {
      updatedRows =>
        if (updatedRows > 0) {
          broadcastService.broadcastToProject(projectId, ColumnUpdated(ColumnUpdatedPayload(columnId, req.name)))
        }
        updatedRows
    }
  }

  private def changeStatus(columnId: Int,
                           userId: Int,
                           validFrom: Set[ColumnStatus],
                           next: ColumnStatus,
                           errorMsg: String,
                           broadcastMessage: OutgoingMessage
                          ): Future[Int] = {
    val action = for {
      maybe <- columnRepository.findStatusAndProjectIdIfUserInProject(
        columnId,
        userId
      )
      (updatedRows, projectId) <- maybe match {
        case Some((projectId, status)) if validFrom.contains(status) =>
          columnRepository.updateStatus(columnId, next)
            .map(rows => (rows, projectId))
        case Some((_, _)) =>
          DBIO.failed(AppException(errorMsg, Status.BAD_REQUEST))
        case None =>
          DBIO.failed(AppException("Column not found", Status.NOT_FOUND))
      }
    } yield (updatedRows, projectId)

    db.run(action).map {
      case (updatedRows, projectId) =>
        if (updatedRows > 0) {
          broadcastService.broadcastToProject(projectId, broadcastMessage)
        }
        updatedRows
    }
  }

    def archiveColumn(columnId: Int, userId: Int): Future[Int] =
        changeStatus(
        columnId,
        userId,
        validFrom = Set(ColumnStatus.active),
        next = ColumnStatus.archived,
        errorMsg = "Only active columns can be archived",
        broadcastMessage = ColumnStatusUpdated(ColumnStatusUpdatedPayload(columnId, ColumnStatus.archived))
        )

    def restoreColumn(columnId: Int, userId: Int): Future[Int] =
        changeStatus(
        columnId,
        userId,
        validFrom = Set(ColumnStatus.archived),
        next = ColumnStatus.active,
        errorMsg = "Only archived columns can be restored",
        broadcastMessage = ColumnStatusUpdated(ColumnStatusUpdatedPayload(columnId, ColumnStatus.active))
        )

    def deleteColumn(columnId: Int, userId: Int): Future[Int] =
        changeStatus(
        columnId,
        userId,
        validFrom = Set(ColumnStatus.archived),
        next = ColumnStatus.deleted,
        errorMsg = "Only archived columns can be deleted",
        broadcastMessage = ColumnStatusUpdated(ColumnStatusUpdatedPayload(columnId, ColumnStatus.deleted))
        )

  def updatePosition(projectId: Int,
                     columnId: Int,
                     request: UpdateColumnPositionRequest,
                     userId: Int): Future[Int] = {
    val action = for {
      maybe <- columnRepository.findStatusAndProjectIdIfUserInProject(
        columnId,
        userId
      )
      updatedRows <- maybe match {
        case Some((_, s)) if s == ColumnStatus.active =>
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
