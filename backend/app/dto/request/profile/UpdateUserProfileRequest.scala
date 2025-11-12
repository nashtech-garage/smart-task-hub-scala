package dto.request.profile

import play.api.libs.json.{Json, Reads, Writes}

case class UpdateUserProfileRequest(userLanguage: Option[String], themeMode: Option[String])

object UpdateUserProfileRequest {
  implicit val reads: Reads[UpdateUserProfileRequest] = Json.reads[UpdateUserProfileRequest]
  implicit val writes: Writes[UpdateUserProfileRequest] =
    Json.writes[UpdateUserProfileRequest]
}