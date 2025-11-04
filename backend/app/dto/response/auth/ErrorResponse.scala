package dto.response.auth

import play.api.libs.json.{Json, OFormat}
import play.api.http.{ContentTypes, Writeable}
import play.api.mvc.Codec

case class ErrorResponse(
  error: String,
  error_description: Option[String] = None
)

object ErrorResponse {
  implicit val format: OFormat[ErrorResponse] = Json.format[ErrorResponse]

  implicit def writeable(implicit codec: Codec): Writeable[ErrorResponse] = {
    Writeable(
      transform = (errorResponse: ErrorResponse) => codec.encode(Json.toJson(errorResponse).toString()),
      contentType = Some(ContentTypes.JSON)
    )
  }
}
