package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class TaskCreated(payload: TaskCreatedPayload) extends BoardMessage {
  override val messageType: String = BoardMessageTypes.TASK_CREATED
}

case class TaskCreatedPayload(taskId: Int,
                              columnId: Int,
                              taskPosition: Int,
                              name: String)
object TaskCreatedPayload {
  implicit val taskCreatedPayloadFormat: OFormat[TaskCreatedPayload] =
    Json.format[TaskCreatedPayload]
}
