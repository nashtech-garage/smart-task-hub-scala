package models.tables

import db.MyPostgresProfile.api._
import models.entities.UserTask
import slick.lifted.Tag

import java.time.LocalDateTime

class UserTaskTable(tag: Tag) extends Table[UserTask](tag, "user_tasks") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def taskId = column[Int]("task_id")

  def assignedTo = column[Int]("assigned_to")

  def assignedBy = column[Option[Int]]("assigned_by")

  def assignedAt = column[LocalDateTime]("assigned_at")

  def * = (id.?, taskId, assignedTo, assignedBy, assignedAt) <> ((UserTask.apply _).tupled, UserTask.unapply)

  def assignedToFk = foreignKey("fk_user_tasks_assigned_to", assignedTo, TableQuery[UserTable])(_.id)

  def assignedByFk = foreignKey("fk_user_tasks_assigned_by", assignedTo, TableQuery[UserTable])(_.id)

  def taskFk = foreignKey("fk_user_tasks_task", assignedTo, TableQuery[UserTable])(_.id)


}
