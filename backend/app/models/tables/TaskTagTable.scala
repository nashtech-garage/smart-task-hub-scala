package models.tables

import models.entities.TaskTag
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class TaskTagTable(tag: Tag) extends Table[TaskTag](tag, "task_tags") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def taskId = column[Option[Int]]("task_id")
  def tagId = column[Option[Int]]("tag_id")

  def * = (id.?, taskId, tagId) <> ((TaskTag.apply _).tupled, TaskTag.unapply)

  def taskTagIndex = index("task_tags_task_id_tag_id_unique", (taskId, tagId), unique = true)
  def taskIdIndex = index("task_tags_task_id_index", taskId)
  def tagIdIndex = index("task_tags_tag_id_index", tagId)

  def taskFk = foreignKey("task_tags_task_id_fkey", taskId, TableQuery[TaskTable])(_.id.?)
  def tagFk = foreignKey("task_tags_tag_id_fkey", tagId, TableQuery[TagTable])(_.id.?)
}
