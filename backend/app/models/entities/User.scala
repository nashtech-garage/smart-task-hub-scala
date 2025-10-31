package models.entities

import models.Enums.UserProjectRole
import models.Enums.UserProjectRole.UserProjectRole
import play.api.libs.json.{Json, OFormat}

import java.time.{Instant, LocalDateTime}

case class Role(id: Option[Int] = None, name: String)

case class User(
    id: Option[Int] = None,
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String] = None,
    roleId: Option[Int] = None,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now()
)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}

case class UserProject(id: Option[Int] = None,
                       userId: Int,
                       projectId: Int,
                       role: UserProjectRole = UserProjectRole.member,
                       invitedBy: Option[Int] = None,
                       joinedAt: Instant = Instant.now())

object UserProject {
  implicit val userProjectFormat: OFormat[UserProject] = Json.format[UserProject]

}