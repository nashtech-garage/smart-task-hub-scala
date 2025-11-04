package dto.websocket.board.messageTypes

import dto.response.task.TaskDetailResponse
import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class TaskUpdated(payload: TaskUpdatedPayload) extends BoardMessage {
  override val messageType: String = BoardMessageTypes.TASK_UPDATED
}

case class TaskUpdatedPayload(taskId: Int,
                              columnId: Int,
                              taskPosition: Int,
                              detail: TaskDetailResponse)
object TaskUpdatedPayload {
  implicit val taskUpdatedPayloadFormat: OFormat[TaskUpdatedPayload] =
    Json.format[TaskUpdatedPayload]
}
