package models.tables

import models.entities.ChecklistItem
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.time.LocalDateTime

class ChecklistItemTable(tag: Tag) extends Table[ChecklistItem](tag, "checklist_items") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def checklistId = column[Option[Int]]("checklist_id")
  def content = column[Option[String]]("content")
  def isCompleted = column[Boolean]("is_completed")
  def createdAt = column[Option[LocalDateTime]]("created_at")
  def updatedAt = column[Option[LocalDateTime]]("updated_at")

  def * = (id.?, checklistId, content, isCompleted, createdAt, updatedAt) <> ((ChecklistItem.apply _).tupled, ChecklistItem.unapply)

  def checklistIdIndex = index("checklist_items_checklist_id_index", checklistId)
  def checklistFk = foreignKey("checklist_items_checklist_id_fkey", checklistId, TableQuery[ChecklistTable])(_.id.?)
}