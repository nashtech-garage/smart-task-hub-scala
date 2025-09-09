package dto.request.task

import models.Enums
import play.api.i18n.Messages
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{notEmpty, validateOptionalField, validateRequiredField}

import java.time.Instant

case class UpdateTaskRequest(
                              name: String,
                              description: Option[String],
                              startDate: Option[Instant],
                              endDate: Option[Instant],
                              priority: Option[Enums.TaskPriority.TaskPriority],
                              isCompleted: Option[Boolean]
                            )

object UpdateTaskRequest {

  implicit def reads(implicit messages: Messages): Reads[UpdateTaskRequest] =
    (validateRequiredField[String](
      "name",
      ErrorMessages.required("Task name"),
      Seq(
        notEmpty(ErrorMessages.empty("Task name"))
      ),
      _.trim
    ) and
      validateOptionalField[String](
        "description",
        Seq(
          notEmpty(ErrorMessages.empty("Task description"))
        ),
        _.trim
      ) and
      validateOptionalField[Instant](
        "startDate",
        Seq(),
        identity
      ) and
      validateOptionalField[Instant](
        "endDate",
        Seq(),
        identity
      ) and
      validateOptionalField[Enums.TaskPriority.TaskPriority](
        "priority",
        Seq(),
        identity
      ) and
      validateOptionalField[Boolean](
        "isCompleted",
        Seq(),
        identity
      ))(UpdateTaskRequest.apply _)

  implicit val writes: Writes[UpdateTaskRequest] =
    Json.writes[UpdateTaskRequest]
}
