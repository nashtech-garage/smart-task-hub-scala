package dto.request.column

import play.api.i18n.Messages
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{notEmpty, validateRequiredField}

case class UpdateColumnRequest(name: String)

object UpdateColumnRequest {

  implicit def reads(implicit messages: Messages): Reads[UpdateColumnRequest] =
    validateRequiredField[String](
      "name",
      ErrorMessages.required("Column name"),
      Seq(notEmpty(ErrorMessages.empty("Column name"))),
      _.trim
    ).map(UpdateColumnRequest.apply)

  implicit val writes: Writes[UpdateColumnRequest] =
    Json.writes[UpdateColumnRequest]
}
