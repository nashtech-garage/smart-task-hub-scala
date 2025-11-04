package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class ColumnMoved(payload: ColumnMovedPayload) extends BoardMessage {
  override val messageType: String = BoardMessageTypes.COLUMN_MOVED
}

case class ColumnMovedPayload(columnId: Int, newPosition: Int)
object ColumnMovedPayload {
  implicit val columnMovedPayloadFormat: OFormat[ColumnMovedPayload] =
    Json.format[ColumnMovedPayload]
}
