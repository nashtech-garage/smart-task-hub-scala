package dto.websocket.board.messageTypes

import dto.response.task.AssignMemberToTaskResponse
import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class MemberAssignedToTask(payload: MemberAssignedToTaskPayload)
    extends BoardMessage {
  override val messageType: String = BoardMessageTypes.MEMBER_ASSIGNED_TO_TASK
}

case class MemberAssignedToTaskPayload(taskId: Int,
                                       assignData: AssignMemberToTaskResponse)
object MemberAssignedToTaskPayload {
  implicit val memberAssignedToTaskPayloadFormat
    : OFormat[MemberAssignedToTaskPayload] =
    Json.format[MemberAssignedToTaskPayload]

}
