package dto.request.task

import play.api.libs.json.{Json, OFormat}

case class AssignMemberRequest(userId: Int)

object AssignMemberRequest {
  implicit val format: OFormat[AssignMemberRequest] = Json.format[AssignMemberRequest]
}
