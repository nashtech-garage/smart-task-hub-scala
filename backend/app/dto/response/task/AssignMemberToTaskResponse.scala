package dto.response.task

import play.api.libs.json.{Json, OFormat}

case class AssignMemberToTaskResponse(userId: Int,
                                      username: String,
                                      columnId: Int)

object AssignMemberToTaskResponse {
    implicit val format: OFormat[AssignMemberToTaskResponse] = Json.format[AssignMemberToTaskResponse]
}
