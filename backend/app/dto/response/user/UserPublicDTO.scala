package dto.response.user

import play.api.libs.json.{Json, OFormat}

case class UserPublicDTO (id: Int, name: String)

object UserPublicDTO {
  implicit val format: OFormat[UserPublicDTO] =
    Json.format[UserPublicDTO]
}
