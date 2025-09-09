package models.tables

import models.entities.Checklist
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.time.LocalDateTime

class ChecklistTable(tag: Tag) extends Table[Checklist](tag, "checklists") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def taskId = column[Option[Int]]("task_id")
  def name = column[Option[String]]("name")
  def createdAt = column[Option[LocalDateTime]]("created_at")
  def updatedAt = column[Option[LocalDateTime]]("updated_at")

  def * = (id.?, taskId, name, createdAt, updatedAt) <> ((Checklist.apply _).tupled, Checklist.unapply)

  def taskIdIndex = index("checklists_task_id_index", taskId)
  def taskFk = foreignKey("checklists_task_id_fkey", taskId, TableQuery[TaskTable])(_.id.?)
}
