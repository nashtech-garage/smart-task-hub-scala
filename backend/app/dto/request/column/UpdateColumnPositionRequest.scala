package dto.request.column

import play.api.i18n.Messages
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{minValue, validateRequiredField}

case class UpdateColumnPositionRequest(position: Int)

object UpdateColumnPositionRequest {
  implicit def reads(implicit messages: Messages): Reads[UpdateColumnPositionRequest] =
    validateRequiredField[Int](
      "position",
      ErrorMessages.required("Position"),
      Seq(minValue(1, ErrorMessages.minValue("Position", 1)))
    ).map(UpdateColumnPositionRequest.apply)

  implicit val writes: Writes[UpdateColumnPositionRequest] =
    Json.writes[UpdateColumnPositionRequest]
}
