package dto.websocket.board

import dto.websocket.OutgoingMessage
import dto.websocket.board.messageTypes._
import play.api.libs.json.{Json, Writes}

trait BoardMessage extends OutgoingMessage{
  def messageType: String
}

object BoardMessage {
  implicit val writes: Writes[BoardMessage] = Writes {
    msg =>
      Json.obj(
        "type" -> msg.messageType,
        "payload" -> (msg match {
          case ColumnMoved(p)          => Json.toJson(p)
          case TaskMoved(p)            => Json.toJson(p)
          case TaskCreated(p)          => Json.toJson(p)
          case MemberAssignedToTask(p) => Json.toJson(p)
          case MemberUnassignedFromTask(p) => Json.toJson(p)
          case TaskUpdated(p)          => Json.toJson(p)
          case TaskStatusUpdated(p)    => Json.toJson(p)
          case ColumnCreated(p)        => Json.toJson(p)
          case ColumnUpdated(p)        => Json.toJson(p)
          case ColumnStatusUpdated(p)    => Json.toJson(p)
        })
      )
  }
}