package dto.websocket

import dto.websocket.board.BoardMessage
import play.api.libs.json.Writes

trait OutgoingMessage

object OutgoingMessage {
  implicit val writes: Writes[OutgoingMessage] = {
    case bm: BoardMessage => BoardMessage.writes.writes(bm)
  }
}
