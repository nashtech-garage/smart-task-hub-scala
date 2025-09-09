package dto.response

import play.api.libs.json._

case class ApiResponse[T](
                           message: String,
                           data: Option[T] = None,
                           errors: Option[List[FieldError]] = None
                         )

object ApiResponse {
  implicit def writes[T](implicit tWrites: Writes[T]): Writes[ApiResponse[T]] = Json.writes[ApiResponse[T]]
  implicit def reads[T](implicit tReads: Reads[T]): Reads[ApiResponse[T]] = Json.reads[ApiResponse[T]]

  def success[T](message: String = "Success", data: T): ApiResponse[T] =
    ApiResponse(message, Some(data))

  def success[T](message: String): ApiResponse[T] =
    ApiResponse(message, None)

  def successNoData(message: String = "Success"): ApiResponse[String] =
    ApiResponse(message, None)

  def error[T](message: String = "Error"): ApiResponse[T] =
    ApiResponse(message, None)

  def errorNoData(message: String = "Error"): ApiResponse[String] =
    ApiResponse(message, None)

  def withData[T](message: String, data: T): ApiResponse[T] =
    ApiResponse(message, Some(data))

  def withoutData[T](message: String): ApiResponse[T] =
    ApiResponse(message, None)
}

// Field error of dto validation
case class FieldError(field: String, message: String)
object FieldError {
  implicit val format: Format[FieldError] = Json.format[FieldError]
}
