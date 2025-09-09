package dto.request.project

import models.Enums.{ProjectStatus, WorkspaceStatus}
import models.Enums.ProjectStatus.ProjectStatus
import play.api.i18n.Messages
import play.api.libs.json.Reads
import utils.ErrorMessages
import validations.CustomValidators.{notEmpty, validateRequiredEnum, validateRequiredField}

case class UpdateProjectStatusRequest(status: ProjectStatus)

object UpdateProjectStatusRequest {
  implicit def reads(implicit messages: Messages): Reads[UpdateProjectStatusRequest] =
    validateRequiredEnum(
      "status",
      ProjectStatus,
      ErrorMessages.required("Project status"),
      ErrorMessages.invalidEnum("Project status", ProjectStatus.values.map(_.toString).toSeq)
    ).map(UpdateProjectStatusRequest.apply)
}
