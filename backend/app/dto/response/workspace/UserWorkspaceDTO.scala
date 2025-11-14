package dto.response.workspace

import play.api.libs.json.{Json, OFormat}

case class UserWorkspaceDTO(
                               userId: Int,
                               name: String,
                               email: String,
                               role: String,
                               joinedAt: Option[java.time.Instant] = None
                           )

object UserWorkspaceDTO {
    implicit val format: OFormat[UserWorkspaceDTO] = Json.format[UserWorkspaceDTO]
}
