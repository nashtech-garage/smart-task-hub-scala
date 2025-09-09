package models.tables

import db.MyPostgresProfile.api._
import models.Enums.UserProjectRole.UserProjectRole
import models.entities.UserProject
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.lifted.Tag

import java.time.Instant

class UserProjectTable(tag: Tag) extends Table[UserProject](tag, "user_projects") {

  implicit val instantColumnType: JdbcType[Instant] with BaseTypedType[Instant] = MappedColumnType.base[Instant, java.sql.Timestamp](
    instant => java.sql.Timestamp.from(instant),
    timestamp => timestamp.toInstant
  )

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Int]("user_id")
  def projectId = column[Int]("project_id")
  def role = column[UserProjectRole]("role")
  def invitedBy = column[Option[Int]]("invited_by")
  def joinedAt = column[Instant]("joined_at")

  def * = (id.?, userId, projectId, role, invitedBy, joinedAt) <> ((UserProject.apply _).tupled, UserProject.unapply)

    def user = foreignKey("fk_user_project_user", userId, TableQuery[UserTable])(_.id)
    def project = foreignKey("fk_user_project_project", projectId, TableQuery[ProjectTable])(_.id)
    def invitedByUser = foreignKey("fk_user_project_invited_by", invitedBy, TableQuery[UserTable])(_.id.?)
}
