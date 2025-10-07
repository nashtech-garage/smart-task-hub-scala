package models.tables

import models.entities.Task
import models.Enums.TaskPriority.TaskPriority
import db.MyPostgresProfile.api._
import models.Enums.TaskStatus.TaskStatus
import slick.lifted.Tag

import java.time.{Instant, LocalDateTime}

class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def columnId = column[Int]("column_id")
  def name = column[String]("name")
  def description = column[Option[String]]("description")
  def startDate = column[Option[Instant]]("start_date")
  def endDate = column[Option[Instant]]("end_date")
  def priority = column[Option[TaskPriority]]("priority")
  def position = column[Int]("position")
  def createdBy = column[Option[Int]]("created_by")
  def updatedBy = column[Option[Int]]("updated_by")
  def createdAt = column[Instant]("created_at")
  def updatedAt = column[Instant]("updated_at")
  def status = column[TaskStatus]("status")
  def isCompleted = column[Boolean]("is_completed", O.Default(false))

  def * =
    (id.?, columnId, name, description, startDate, endDate, priority, position,
      createdBy, updatedBy, createdAt, updatedAt, status, isCompleted) <>
    ((Task.apply _).tupled, Task.unapply)

  def columnPositionIndex = index("tasks_column_id_position_index", (columnId, position))
  def createdByIndex = index("tasks_created_by_index", createdBy)
  def startDateIndex = index("tasks_start_date_index", startDate)
  def endDateIndex = index("tasks_end_date_index", endDate)

  def columnFk = foreignKey("tasks_column_id_fkey", columnId, TableQuery[ColumnTable])(_.id)
  def createdByFk = foreignKey("tasks_created_by_fkey", createdBy, TableQuery[UserTable])(_.id.?)
  def updatedByFk = foreignKey("tasks_updated_by_fkey", updatedBy, TableQuery[UserTable])(_.id.?)
}
