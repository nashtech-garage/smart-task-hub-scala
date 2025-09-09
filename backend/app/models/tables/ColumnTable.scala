package models.tables

import models.entities.Column
import db.MyPostgresProfile.api._
import models.Enums.ColumnStatus.ColumnStatus
import slick.lifted.Tag

import java.time.Instant

class ColumnTable(tag: Tag) extends Table[Column](tag, "columns") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def projectId = column[Int]("project_id")
  def name = column[String]("name", O.Unique)
  def position = column[Int]("position", O.Unique)
  def createdAt = column[Instant]("created_at")
  def updatedAt = column[Instant]("updated_at")
  def status = column[ColumnStatus]("status")

  def * = (id.?, projectId, name, position, createdAt, updatedAt, status) <> ((Column.apply _).tupled, Column.unapply)

  def projectPositionIndex = index("columns_project_id_position_index", (projectId, position))
  def projectIdIndex = index("columns_project_id_index", projectId)

  def projectFk = foreignKey("columns_project_id_fkey", projectId, TableQuery[ProjectTable])(_.id)
}
