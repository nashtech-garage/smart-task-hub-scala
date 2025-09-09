package models.tables

import models.entities.Notification
import models.Enums.NotificationType.NotificationType
import db.MyPostgresProfile.api._
import slick.lifted.Tag

import java.time.LocalDateTime

class NotificationTable(tag: Tag) extends Table[Notification](tag, "notifications") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Option[Int]]("user_id")
  def taskId = column[Option[Int]]("task_id")
  def notificationType = column[Option[NotificationType]]("type")
  def message = column[Option[String]]("message")
  def isRead = column[Boolean]("is_read")
  def createdAt = column[Option[LocalDateTime]]("created_at")

  def * = (id.?, userId, taskId, notificationType, message, isRead, createdAt) <> ((Notification.apply _).tupled, Notification.unapply)

  def userReadCreatedIndex = index("notifications_user_id_is_read_created_at_index", (userId, isRead, createdAt))
  def userIdIndex = index("notifications_user_id_index", userId)
  def taskIdIndex = index("notifications_task_id_index", taskId)

  def userFk = foreignKey("notifications_user_id_fkey", userId, TableQuery[UserTable])(_.id.?)
  def taskFk = foreignKey("notifications_task_id_fkey", taskId, TableQuery[TaskTable])(_.id.?)
}