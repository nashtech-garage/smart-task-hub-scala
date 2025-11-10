package models.entities

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class UserProfile(
  id: Option[Int] = None,
  userId: Int,
  userLanguage: String,
  themeMode: String,
  createdAt: LocalDateTime = LocalDateTime.now(),
  updatedAt: LocalDateTime = LocalDateTime.now()
)

object UserProfile {
  implicit val userProfileFormat: OFormat[Project] = Json.format[Project]
}
