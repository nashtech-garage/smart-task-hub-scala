package dto.request.workspace

import play.api.i18n.Messages
import play.api.libs.json.Reads
import utils.ErrorMessages
import validations.CustomValidators.{regexMatch, validateRequiredField}

case class InviteUserIntoWorkspaceRequest(email: String)

object InviteUserIntoWorkspaceRequest {
  private val emailRegex: String =
    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"

  implicit def reads(
    implicit messages: Messages
  ): Reads[InviteUserIntoWorkspaceRequest] =
    validateRequiredField[String](
      "email",
      ErrorMessages.required("email"),
      validations = Seq(
        regexMatch(
          emailRegex,
          ErrorMessages.invalidFormat("email")
        )
      ),
      _.trim
    ).map(InviteUserIntoWorkspaceRequest.apply)
}
