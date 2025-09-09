package dto.response.auth

import play.api.libs.json.{Format, Json}

case class AuthResponse (
    id: Int,
    name: String,
    email: String
)

object AuthResponse {
    implicit val format: Format[AuthResponse] = Json.format[AuthResponse]
}
