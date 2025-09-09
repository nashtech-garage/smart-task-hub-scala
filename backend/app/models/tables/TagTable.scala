package models.tables

import models.entities.Tag
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{Tag => SlickTag}
import java.time.LocalDateTime

class TagTable(tag: SlickTag) extends Table[Tag](tag, "tags") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def projectId = column[Option[Int]]("project_id")
  def name = column[Option[String]]("name")
  def color = column[Option[String]]("color")
  def createdAt = column[Option[LocalDateTime]]("created_at")

  def * = (id.?, projectId, name, color, createdAt) <> ((Tag.apply _).tupled, Tag.unapply)

  def projectIdIndex = index("tags_project_id_index", projectId)
  def projectNameIndex = index("tags_project_id_name_unique", (projectId, name), unique = true)

  def projectFk = foreignKey("tags_project_id_fkey", projectId, TableQuery[ProjectTable])(_.id.?)
}
