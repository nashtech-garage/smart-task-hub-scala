package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import models.Enums.ColumnStatus.ColumnStatus
import play.api.libs.json.{Json, OFormat}

case class ColumnStatusUpdated(payload: ColumnStatusUpdatedPayload)
  extends BoardMessage {
  override val messageType: String = BoardMessageTypes.COLUMN_STATUS_UPDATED
}

case class ColumnStatusUpdatedPayload(columnId: Int,
                                    updatedStatus: ColumnStatus)
object ColumnStatusUpdatedPayload {
  implicit val columnStatusUpdatedPayloadFormat
  : OFormat[ColumnStatusUpdatedPayload] = Json.format[ColumnStatusUpdatedPayload]
}
