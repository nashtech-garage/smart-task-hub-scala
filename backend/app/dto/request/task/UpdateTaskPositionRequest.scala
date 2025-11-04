package dto.request.task

import play.api.i18n.Messages
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{minValue, validateRequiredField}

case class UpdateTaskPositionRequest(position: Int, columnId: Int)

object UpdateTaskPositionRequest {
  implicit def reads(
    implicit messages: Messages
  ): Reads[UpdateTaskPositionRequest] =
    (validateRequiredField[Int](
      "position",
      ErrorMessages.required("Position"),
      Seq(minValue(1, ErrorMessages.minValue("Position", 1)))
    ) and validateRequiredField[Int](
      "columnId",
      ErrorMessages.required("columnId"),
      Seq(minValue(1, ErrorMessages.minValue("Position", 1)))
    ))(UpdateTaskPositionRequest.apply _)

  implicit val writes: Writes[UpdateTaskPositionRequest] =
    Json.writes[UpdateTaskPositionRequest]
}
