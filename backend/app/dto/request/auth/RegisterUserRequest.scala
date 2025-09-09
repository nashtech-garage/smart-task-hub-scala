package dto.request.auth

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{email, maxLength, minLength}

case class RegisterUserRequest(
                                name: String,
                                email: String,
                                password: String
                              )

object RegisterUserRequest {
  private val NameMinLength = 1
  private val NameMaxLength = 100
  private val PasswordMinLength = 6
  private val PasswordMaxLength = 50

  implicit val reads: Reads[RegisterUserRequest] = (
    (JsPath \ "name").read[String](minLength[String](NameMinLength) keepAnd maxLength[String](NameMaxLength)).map(_.trim) and
      (JsPath \ "email").read[String](email).map(_.trim.toLowerCase) and
      (JsPath \ "password").read[String](minLength[String](PasswordMinLength) keepAnd maxLength[String](PasswordMaxLength))
    )(RegisterUserRequest.apply _)

  implicit val writes: Writes[RegisterUserRequest] = Json.writes[RegisterUserRequest]
}
