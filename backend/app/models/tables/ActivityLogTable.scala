package models.tables

import models.entities.ActivityLog
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag
import java.time.LocalDateTime

class ActivityLogTable(tag: Tag) extends Table[ActivityLog](tag, "activity_logs") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Option[Int]]("user_id")
  def projectId = column[Option[Int]]("project_id")
  def taskId = column[Option[Int]]("task_id")
  def action = column[Option[String]]("action")
  def content = column[Option[String]]("content")
  def createdAt = column[Option[LocalDateTime]]("created_at")

  def * = (id.?, userId, projectId, taskId, action, content, createdAt) <> ((ActivityLog.apply _).tupled, ActivityLog.unapply)

  def projectCreatedAtIndex = index("activity_logs_project_id_created_at_index", (projectId, createdAt))
  def userIdIndex = index("activity_logs_user_id_index", userId)
  def taskIdIndex = index("activity_logs_task_id_index", taskId)
  def createdAtIndex = index("activity_logs_created_at_index", createdAt)

  def userFk = foreignKey("activity_logs_user_id_fkey", userId, TableQuery[UserTable])(_.id.?)
  def projectFk = foreignKey("activity_logs_project_id_fkey", projectId, TableQuery[ProjectTable])(_.id.?)
  def taskFk = foreignKey("activity_logs_task_id_fkey", taskId, TableQuery[TaskTable])(_.id.?)
}
