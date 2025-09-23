package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class ColumnUpdated(payload: ColumnUpdatedPayload) extends BoardMessage {
  override val messageType: String = BoardMessageTypes.COLUMN_UPDATED
}

case class ColumnUpdatedPayload(columnId: Int,
                                name: String)
object ColumnUpdatedPayload {
  implicit val columnUpdatedPayloadFormat: OFormat[ColumnUpdatedPayload] =
    Json.format[ColumnUpdatedPayload]
}


