package models.tables

import models.entities.Role
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class RoleTable(tag: Tag) extends Table[Role](tag, "roles") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> ((Role.apply _).tupled, Role.unapply)

  def nameIndex = index("roles_name_unique", name, unique = true)
}