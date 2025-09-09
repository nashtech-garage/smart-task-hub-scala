package models.tables

import models.entities.TaskComment
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.time.LocalDateTime

class TaskCommentTable(tag: Tag) extends Table[TaskComment](tag, "task_comments") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def taskId = column[Option[Int]]("task_id")

  def userId = column[Option[Int]]("user_id")

  def content = column[Option[String]]("content")

  def createdAt = column[Option[LocalDateTime]]("created_at")

  def updatedAt = column[Option[LocalDateTime]]("updated_at")

  def * = (id.?, taskId, userId, content, createdAt, updatedAt) <> ((TaskComment.apply _).tupled, TaskComment.unapply)

  def taskCreatedAtIndex = index("task_comments_task_id_created_at_index", (taskId, createdAt))

  def taskIdIndex = index("task_comments_task_id_index", taskId)

  def userIdIndex = index("task_comments_user_id_index", userId)

  def taskFk = foreignKey("task_comments_task_id_fkey", taskId, TableQuery[TaskTable])(_.id.?)

  def userFk = foreignKey("task_comments_user_id_fkey", userId, TableQuery[UserTable])(_.id.?)
}