package dto.websocket.board.messageTypes

import dto.websocket.board.{BoardMessage, BoardMessageTypes}
import play.api.libs.json.{Json, OFormat}

case class ColumnCreated(payload: ColumnCreatedPayload) extends BoardMessage {
  override val messageType: String = BoardMessageTypes.COLUMN_CREATED
}

case class ColumnCreatedPayload(columnId: Int,
                                position: Int,
                                name: String)
object ColumnCreatedPayload {
  implicit val columnCreatedPayloadFormat: OFormat[ColumnCreatedPayload] =
    Json.format[ColumnCreatedPayload]
}
