package models.entities

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class UserProfile(
  id: Int,
  userId: Int,
  userLanguage: String,
  themeMode: String,
  createdAt: LocalDateTime = LocalDateTime.now(),
  updatedAt: LocalDateTime = LocalDateTime.now(),
  createdBy: Option[Int] = None,
  updatedBy: Option[Int] = None
)

object UserProfile {
  implicit val userProfileFormat: OFormat[Project] = Json.format[Project]
  def apply(id: Int, userId: Int, userLanguage: String, themeMode: String, createdAt: LocalDateTime, updatedAt: LocalDateTime): UserProfile =
    UserProfile(id, userId, userLanguage, themeMode, createdAt, updatedAt, None, None)
}
