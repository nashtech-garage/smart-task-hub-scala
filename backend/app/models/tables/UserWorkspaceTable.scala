package models.tables

import models.entities.UserWorkspace
import models.Enums.UserWorkspaceRole.UserWorkspaceRole
import models.Enums.UserWorkspaceStatus.UserWorkspaceStatus
import db.MyPostgresProfile.api._
import slick.lifted.Tag

import java.time.{Instant, LocalDateTime}

class UserWorkspaceTable(tag: Tag) extends Table[UserWorkspace](tag, "user_workspaces") {

  // Custom column type for Instant
  implicit val instantColumnType = MappedColumnType.base[Instant, java.sql.Timestamp](
    instant => java.sql.Timestamp.from(instant),
    timestamp => timestamp.toInstant
  )

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Option[Int]]("user_id")
  def workspaceId = column[Option[Int]]("workspace_id")
  def role = column[Option[UserWorkspaceRole]]("role")
  def status = column[UserWorkspaceStatus]("status")
  def invitedBy = column[Option[Int]]("invited_by")
  def joinedAt = column[Option[Instant]]("joined_at")

  def * = (id.?, userId, workspaceId, role, status, invitedBy, joinedAt) <> ((UserWorkspace.apply _).tupled, UserWorkspace.unapply)

  def userWorkspaceIndex = index("user_workspaces_user_id_workspace_id_unique", (userId, workspaceId), unique = true)
  def userIdIndex = index("user_workspaces_user_id_index", userId)
  def workspaceIdIndex = index("user_workspaces_workspace_id_index", workspaceId)

  def userFk = foreignKey("user_workspaces_user_id_fkey", userId, TableQuery[UserTable])(_.id.?)
  def workspaceFk = foreignKey("user_workspaces_workspace_id_fkey", workspaceId, TableQuery[WorkspaceTable])(_.id.?)
  def invitedByFk = foreignKey("user_workspaces_invited_by_fkey", invitedBy, TableQuery[UserTable])(_.id.?)
}
