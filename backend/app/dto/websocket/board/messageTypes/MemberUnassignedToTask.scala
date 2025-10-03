package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class MemberUnassignedFromTask(payload: MemberUnassignedFromTaskPayload)
    extends BoardMessage {
  override val messageType: String = BoardMessageTypes.MEMBER_UNASSIGNED_TO_TASK
}

case class MemberUnassignedFromTaskPayload(
  taskId: Int,
  userId: Int
)
object MemberUnassignedFromTaskPayload {
  implicit val memberAssignedToTaskPayloadFormat
    : OFormat[MemberUnassignedFromTaskPayload] =
    Json.format[MemberUnassignedFromTaskPayload]
}
