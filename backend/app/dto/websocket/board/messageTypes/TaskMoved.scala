package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class TaskMoved(payload: TaskMovedPayload) extends BoardMessage {
  override val messageType: String = BoardMessageTypes.TASK_MOVED
}

case class TaskMovedPayload(taskId: Int,
                            fromColumnId: Int,
                            toColumnId: Int,
                            newPosition: Int)
object TaskMovedPayload {
  implicit val taskMovedPayloadFormat: OFormat[TaskMovedPayload] =
    Json.format[TaskMovedPayload]
}
