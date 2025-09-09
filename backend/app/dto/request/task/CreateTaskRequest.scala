package dto.request.task

import play.api.i18n.Messages
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{notEmpty, validateRequiredField}

case class CreateTaskRequest(name: String, position: Int)

object CreateTaskRequest {

  implicit def reads(implicit messages: Messages): Reads[CreateTaskRequest] =
    (validateRequiredField[String](
      "name",
      ErrorMessages.required("Task name"),
      Seq(
        notEmpty(ErrorMessages.empty("Task name"))
      ),
      _.trim
    ) and
      validateRequiredField[Int](
        "position",
        ErrorMessages.required("Position"),
        Seq()
      ))(CreateTaskRequest.apply _)

  implicit val writes: Writes[CreateTaskRequest] =
    Json.writes[CreateTaskRequest]
}