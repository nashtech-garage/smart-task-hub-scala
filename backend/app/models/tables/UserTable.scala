package models.tables

import models.entities.User
import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def password = column[String]("password")
  def avatarUrl = column[Option[String]]("avatar_url")
  def roleId = column[Option[Int]]("role_id")
  def createdAt = column[LocalDateTime]("created_at")
  def updatedAt = column[LocalDateTime]("updated_at")

  def * = (id.?, name, email, password, avatarUrl, roleId, createdAt, updatedAt) <> ((User.apply _).tupled, User.unapply)

  def emailIndex = index("user-email_unique", email, unique = true)
  def roleFk = foreignKey("user_role_id_fkey", roleId, TableQuery[RoleTable])(_.id.?)
}
