package dto.response.user

import play.api.libs.json.{Json, OFormat}

case class UserInProjectResponse(id: Int, name: String)
object UserInProjectResponse {
  implicit val format: OFormat[UserInProjectResponse] =
    Json.format[UserInProjectResponse]
}
