package dto.request.workspace

import play.api.i18n.Messages
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{minLength, notEmpty, validateOptionalField, validateRequiredField}

case class CreateWorkspaceRequest(name: String,
                                  description: Option[String] = None)

object CreateWorkspaceRequest {

  implicit def reads(
    implicit messages: Messages
  ): Reads[CreateWorkspaceRequest] =
    (validateRequiredField[String](
      "name",
      ErrorMessages.required("Workspace name"),
      Seq(
        notEmpty(ErrorMessages.empty("Workspace name")),
        minLength(3, ErrorMessages.tooShort("Workspace name", 3))
      ),
      _.trim
    ) and
      validateOptionalField[String](
        "description",
        Seq(notEmpty(ErrorMessages.empty("Workspace description"))),
        _.trim
      ))(CreateWorkspaceRequest.apply _)

  implicit val writes: Writes[CreateWorkspaceRequest] =
    Json.writes[CreateWorkspaceRequest]
}
