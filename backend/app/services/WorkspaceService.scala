package services

import dto.request.workspace.{CreateWorkspaceRequest, InviteUserIntoWorkspaceRequest, UpdateWorkspaceRequest}
import dto.response.workspace.WorkspaceResponse
import exception.AppException
import mappers.WorkspaceMapper
import models.Enums.UserWorkspaceRole
import models.entities.{UserWorkspace, Workspace}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import repositories.{UserRepository, WorkspaceRepository}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import exception.AppException

@Singleton
class WorkspaceService @Inject()(
  workspaceRepo: WorkspaceRepository,
  userRepo: UserRepository,
  emailService: EmailService,
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  /** Get all workspaces for a specific user */
  def getAllWorkspaces(userId: Int): Future[Seq[WorkspaceResponse]] = {
    val query = workspaceRepo.getWorkspacesForUserQuerySimple(userId)
    db.run(query.result).map(WorkspaceMapper.toResponses)
  }

  /** Get workspace by ID if user has access */
  def getWorkspaceById(id: Int,
                       userId: Int): Future[Option[WorkspaceResponse]] = {
    val query = workspaceRepo.getWorkspaceForUserQuery(id, userId)
    db.run(query.result.headOption).map(_.map(WorkspaceMapper.toResponse))
  }

  /** Delete a workspace by ID if user has admin access */
  def deleteWorkspace(id: Int, userId: Int): Future[Boolean] = {
    val action = workspaceRepo.deleteWorkspaceForUserAction(id, userId)
    db.run(action).map(_ > 0)
  }

  /**
    * Creates a new workspace and assigns the creator as an admin.
    * This operation is performed within a transaction.
    */
  def createWorkspace(workspace: CreateWorkspaceRequest,
                      createdBy: Int): Future[Int] = {
    val now = Instant.now()
    val newWorkspace = Workspace(
      name = workspace.name,
      description = workspace.description,
      createdBy = Some(createdBy),
      createdAt = Some(now),
      updatedAt = Some(now)
    )

    val action = for {
      isWorkspaceNameExists <- workspaceRepo.isWorkspaceNameUsedByUser(
        workspace.name,
        createdBy
      )
      workspaceId <- if (isWorkspaceNameExists) {
        DBIO.failed(
          AppException(
            message = "Workspace name already exists",
            statusCode = Status.CONFLICT
          )
        )
      } else {
        workspaceRepo.createWithOwnerAction(newWorkspace, createdBy)
      }
    } yield workspaceId
    db.run(action.transactionally)
  }

  /**
    * Updates an existing workspace.
    * First checks if the workspace exists, then performs the update.
    */
  def updateWorkspace(id: Int,
                      workspace: UpdateWorkspaceRequest,
                      updatedBy: Int): Future[Int] = {
    val now = Instant.now()

    // Create a transactional action that checks existence and updates
    val action = for {
      // Check if workspace exists
      existingWorkspaceOpt <- workspaceRepo.getByIdQuery(id).result.headOption

      ws <- existingWorkspaceOpt match {
        case Some(existingWorkspace) => DBIO.successful(existingWorkspace)
        case None =>
          DBIO.failed(
            AppException(
              message = "Workspace not found",
              statusCode = Status.NOT_FOUND
            )
          )
      }

      // Check if the new name is already used by the user (if name is changing)
      isWorkspaceNameExists <- workspaceRepo.isWorkspaceNameUsedByUser(
        workspace.name,
        updatedBy
      )
      result <- if (isWorkspaceNameExists) {
        DBIO.failed(
          AppException(
            message = "Duplicate workspace name",
            statusCode = Status.CONFLICT
          )
        )
      } else {
        val updatedWorkspace = ws.copy(
          name = workspace.name,
          description = workspace.description,
          updatedBy = Some(updatedBy),
          updatedAt = Some(now)
        )
        workspaceRepo.updateAction(updatedWorkspace)
      }
    } yield result

    db.run(action.transactionally)
  }

  def inviteUserToWorkspace(inviterId: Int,
                            workspaceId: Int,
                            inviteUserIntoWorkspaceRequest: InviteUserIntoWorkspaceRequest): Future[Int] = {
    val to = inviteUserIntoWorkspaceRequest.email
    db.run(workspaceRepo.isUserInActiveWorkspace(workspaceId, inviterId))
      .flatMap { inviterAllowed =>
        if (!inviterAllowed) {
          throw AppException(
            "Workspace not found or you are not a member",
            Status.NOT_FOUND
          )
        }

        userRepo.findByEmail(to).flatMap {
          case None =>
            throw AppException("User not found", Status.NOT_FOUND)

          case Some(user) =>
            val userId = user.id.get

            db.run(workspaceRepo.isUserInActiveWorkspace(workspaceId, userId))
              .flatMap { alreadyMember =>
                if (alreadyMember) {
                  throw AppException(
                    "User is already a member of the workspace",
                    Status.BAD_REQUEST
                  )
                }

                val userWorkspace = UserWorkspace(
                  userId = userId,
                  workspaceId = workspaceId,
                  role = UserWorkspaceRole.member
                )

                workspaceRepo.insertUserIntoWorkspace(userWorkspace).map { insertedId =>
                  emailService.sendInviteToWorkspaceEmail(to, workspaceId)
                  insertedId
                }
              }
        }
      }
  }
}
