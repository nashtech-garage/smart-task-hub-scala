package dto.request.project

import play.api.i18n.Messages
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes}
import utils.ErrorMessages
import validations.CustomValidators.{
  minLength,
  notEmpty,
  validateOptionalField,
  validateRequiredField
}

case class CreateProjectRequest(name: String,
                                visibility: Option[String] = Some("workspace"))

object CreateProjectRequest {

  implicit def reads(implicit messages: Messages): Reads[CreateProjectRequest] =
    (
      validateRequiredField[String](
        "name",
        ErrorMessages.required("Workspace name"),
        Seq(
          notEmpty(ErrorMessages.empty("Workspace name")),
          minLength(3, ErrorMessages.tooShort("Workspace name", 3))
        ),
        _.trim
      ) and
        validateOptionalField[String]("visibility")
    )(CreateProjectRequest.apply _)

  implicit val writes: Writes[CreateProjectRequest] =
    Json.writes[CreateProjectRequest]
}
