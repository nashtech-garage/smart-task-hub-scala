package models.tables

import db.MyPostgresProfile.api._
import models.Enums.UserWorkspaceRole.UserWorkspaceRole
import models.Enums.UserWorkspaceStatus.UserWorkspaceStatus
import models.entities.UserWorkspace
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.lifted.Tag

import java.time.Instant

class UserWorkspaceTable(tag: Tag)
    extends Table[UserWorkspace](tag, "user_workspaces") {

  // Custom column type for Instant
  implicit val instantColumnType
    : JdbcType[Instant] with BaseTypedType[Instant] =
    MappedColumnType.base[Instant, java.sql.Timestamp](
      instant => java.sql.Timestamp.from(instant),
      timestamp => timestamp.toInstant
    )

  def * =
    (id.?, userId, workspaceId, role, status, invitedBy, joinedAt) <> ((UserWorkspace.apply _).tupled, UserWorkspace.unapply)

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def role = column[UserWorkspaceRole]("role")

  def status = column[UserWorkspaceStatus]("status")

  def joinedAt = column[Instant]("joined_at")

  def userWorkspaceIndex =
    index(
      "user_workspaces_user_id_workspace_id_unique",
      (userId, workspaceId),
      unique = true
    )

  def userIdIndex = index("user_workspaces_user_id_index", userId)

  def userId = column[Int]("user_id")

  def workspaceIdIndex =
    index("user_workspaces_workspace_id_index", workspaceId)

  def userFk =
    foreignKey("user_workspaces_user_id_fkey", userId, TableQuery[UserTable])(
      _.id
    )

  def workspaceFk =
    foreignKey(
      "user_workspaces_workspace_id_fkey",
      workspaceId,
      TableQuery[WorkspaceTable]
    )(_.id)

  def workspaceId = column[Int]("workspace_id")

  def invitedByFk =
    foreignKey(
      "user_workspaces_invited_by_fkey",
      invitedBy,
      TableQuery[UserTable]
    )(_.id.?)

  def invitedBy = column[Option[Int]]("invited_by")
}
