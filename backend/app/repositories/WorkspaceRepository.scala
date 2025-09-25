package repositories

import db.MyPostgresProfile.api.userWorkspaceRoleTypeMapper
import models.Enums
import models.entities.{UserWorkspace, Workspace}
import models.tables.{UserWorkspaceTable, WorkspaceTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WorkspaceRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Table query objects
  private val workspaces = TableQuery[WorkspaceTable]
  private val userWorkspaces = TableQuery[UserWorkspaceTable]

  /**
    * Creates a transactional action that creates a new workspace and associates it with the specified user as an owner.
    * This method returns a database action that performs the creation of both the workspace and the corresponding
    * UserWorkspace entry in a single transaction.
    *
    * @param workspace The Workspace entity to create.
    * @param userId The ID of the user who will own the workspace.
    * @return A database action that returns the ID of the created workspace.
    */
  def createWithOwnerAction(workspace: Workspace, userId: Int): DBIO[Int] = {
    for {
      // Create workspace
      wsId <- (workspaces returning workspaces.map(_.id)) += workspace

      // Create corresponding UserWorkspace entry
      _ <- userWorkspaces += UserWorkspace(
        workspaceId = Some(wsId),
        userId = Some(userId),
        role = Some(Enums.UserWorkspaceRole.admin)
      )
    } yield wsId
  }

  /** Get a workspace by ID query */
  def getByIdQuery(id: Int): Query[WorkspaceTable, Workspace, Seq] =
    workspaces.filter(_.id === id)

  /**
    * Creates an update action for an existing workspace.
    * This method returns a database action that updates the workspace details based on the provided Workspace entity.
    * @param workspace The Workspace entity containing updated data.
    *                  The ID of the workspace must be set in the entity.
    * @return A database action that returns the number of rows affected by the update operation.
    *         If the workspace with the specified ID does not exist, it will return 0.
    *         If the update is successful, it will return 1.
    */
  def updateAction(workspace: Workspace): DBIO[Int] = {
    workspaces.filter(_.id === workspace.id).update(workspace)
  }

  /** Get workspaces for user with just workspace data */
  def getWorkspacesForUserQuerySimple(
    userId: Int
  ): Query[WorkspaceTable, Workspace, Seq] = {
    workspaces
      .join(userWorkspaces)
      .on(_.id === _.workspaceId)
      .filter(_._2.userId === userId)
      .map(_._1) // Return only workspaces
  }

  /** Get a specific workspace for a user if they have access */
  def getWorkspaceForUserQuery(
    workspaceId: Int,
    userId: Int
  ): Query[WorkspaceTable, Workspace, Seq] = {
    workspaces
      .join(userWorkspaces)
      .on(_.id === _.workspaceId)
      .filter {
        case (workspace, userWorkspace) =>
          workspace.id === workspaceId
//                && userWorkspace.userId === userId
      }
      .map(_._1) // Return only the workspace
  }

  /** Delete a workspace only if user has admin access */
  def deleteWorkspaceForUserAction(workspaceId: Int, userId: Int): DBIO[Int] = {
    // First check if user has admin access to this workspace
    val hasAdminAccess = userWorkspaces
      .filter(
        uw =>
          uw.workspaceId === workspaceId &&
            uw.userId === userId
//                && uw.role === Enums.UserWorkspaceRole.admin
      )
      .exists

    for {
      // Check if user has admin access
      hasAccess <- hasAdminAccess.result

      // Delete only if user has admin access
      deleteCount <- if (hasAccess) {
        // Delete UserWorkspace entries first (foreign key constraint)
        userWorkspaces
          .filter(_.workspaceId === workspaceId)
          .delete
          .andThen(
            // Then delete the workspace
            workspaces.filter(_.id === workspaceId).delete
          )
      } else {
        DBIO.successful(0) // Return 0 if no access (could also throw exception)
      }
    } yield deleteCount
  }

  def isUserInActiveWorkspace(workspaceId: Int, userId: Int): DBIO[Boolean] = {
    (for {
      uw <- userWorkspaces if uw.workspaceId === workspaceId && uw.userId === userId
      w  <- workspaces if w.id === workspaceId && !w.isDeleted
    } yield ()).exists.result
  }

  /** Check if a workspace name is already used by a user (case insensitive) */
  def isWorkspaceNameUsedByUser(name: String, userId: Int): DBIO[Boolean] = {
    (for {
      uw <- userWorkspaces if uw.userId === userId && uw.role === Enums.UserWorkspaceRole.admin
      w  <- workspaces if w.id === uw.workspaceId && w.name.toLowerCase === name.toLowerCase && !w.isDeleted
    } yield ()).exists.result
  }

  def insertWorkspaceBatch(workspaceList: Seq[Workspace]): Future[Seq[Int]] = {
    val insertQuery = workspaces returning workspaces.map(_.id)
    val action = insertQuery ++= workspaceList
    db.run(action)
  }

  def insertUserBatchIntoWorkspace(entries: Seq[UserWorkspace]): Future[Seq[Int]] = {
    val insertQuery = userWorkspaces returning userWorkspaces.map(_.id)
    val action = insertQuery ++= entries
    db.run(action)
  }
}
