package dto.response.task

import play.api.libs.json.{Format, Json}

case class AssignedMemberResponse(
                                   id: Int,
                                   name: String
                                 )

object AssignedMemberResponse {
  implicit val format: Format[AssignedMemberResponse] = Json.format[AssignedMemberResponse]
}
