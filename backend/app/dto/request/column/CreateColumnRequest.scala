package dto.request.column

import play.api.i18n.Messages
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{minValue, notEmpty, validateRequiredField}

case class CreateColumnRequest(name: String, position: Int)

object CreateColumnRequest {

  implicit def reads(implicit messages: Messages): Reads[CreateColumnRequest] =
    (validateRequiredField[String](
      "name",
      ErrorMessages.required("Column name"),
      Seq(notEmpty(ErrorMessages.empty("Column name"))),
      _.trim
    ) and
      validateRequiredField[Int](
        "position",
        ErrorMessages.required("Position"),
        Seq(minValue(1, ErrorMessages.minValue("Position", 1)))
      ))(CreateColumnRequest.apply _)

  implicit val writes: Writes[CreateColumnRequest] =
    Json.writes[CreateColumnRequest]
}
