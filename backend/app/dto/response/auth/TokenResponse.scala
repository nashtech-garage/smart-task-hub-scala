package dto.response.auth

import play.api.libs.json.{Json, OFormat}

case class TokenResponse(
  access_token: String,
  expires_in: Int,
  refresh_expires_in: Int,
  refresh_token: String,
  token_type: String,
  session_state: Option[String],
  scope: String
)

object TokenResponse {
  implicit val format: OFormat[TokenResponse] = Json.format[TokenResponse]
}