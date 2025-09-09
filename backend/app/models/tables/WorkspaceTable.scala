package models.tables

import models.entities.Workspace
import slick.lifted.Tag
import db.MyPostgresProfile.api._
import models.Enums.WorkspaceStatus.WorkspaceStatus

import java.time.{Instant, LocalDateTime}

class WorkspaceTable(tag: Tag) extends Table[Workspace](tag, "workspaces") {

  // Custom column type for Instant
  implicit val instantColumnType = MappedColumnType.base[Instant, java.sql.Timestamp](
    instant => java.sql.Timestamp.from(instant),
    timestamp => timestamp.toInstant
  )

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def description = column[Option[String]]("description")

  def status = column[WorkspaceStatus]("status")

  def createdBy = column[Option[Int]]("created_by")

  def createdAt = column[Option[Instant]]("created_at")

  def updatedAt = column[Option[Instant]]("updated_at")

  def updatedBy = column[Option[Int]]("updated_by")

  def isDeleted = column[Boolean]("is_deleted", O.Default(false))

  def * = (id.?, name, description, status, createdBy, createdAt, updatedAt, updatedBy, isDeleted) <> ((Workspace.apply _).tupled, Workspace.unapply)

}
