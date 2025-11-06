package dto.response.user

import play.api.http.{ContentTypes, Writeable}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Codec

case class UserGoogleResponse(
  sub: String,
  name: String,
  given_name: String,
  family_name: String,
  email: String,
  email_verified: Boolean
)

object UserGoogleResponse {
  implicit val format: OFormat[UserGoogleResponse] = Json.format[UserGoogleResponse]
  implicit def writeable(implicit codec: Codec): Writeable[UserGoogleResponse] = {
    Writeable(
      transform = (errorResponse: UserGoogleResponse) => codec.encode(Json.toJson(errorResponse).toString()),
      contentType = Some(ContentTypes.JSON)
    )
  }
}