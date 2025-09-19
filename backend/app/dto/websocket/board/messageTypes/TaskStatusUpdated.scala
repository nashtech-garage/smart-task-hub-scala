package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import models.Enums.TaskStatus.TaskStatus
import play.api.libs.json.{Json, OFormat}

case class TaskStatusUpdated(payload: TaskStatusUpdatedPayload)
    extends BoardMessage {
  override val messageType: String = BoardMessageTypes.TASK_STATUS_UPDATED
}

case class TaskStatusUpdatedPayload(taskId: Int,
                                    columnId: Int,
                                    taskPosition: Int,
                                    name: String,
                                    updatedStatus: TaskStatus)
object TaskStatusUpdatedPayload {
  implicit val taskStatusUpdatedPayloadFormat
    : OFormat[TaskStatusUpdatedPayload] = Json.format[TaskStatusUpdatedPayload]
}
