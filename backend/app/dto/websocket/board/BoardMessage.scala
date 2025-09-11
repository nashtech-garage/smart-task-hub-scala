package dto.websocket.board

import play.api.libs.json._
import dto.websocket.OutgoingMessage

sealed trait BoardMessage extends OutgoingMessage

case class ColumnMoved(columnId: Int, newPosition: Int) extends BoardMessage
case class TaskMoved(taskId: Int, fromColumnId: Int, toColumnId: Int, newPosition: Int) extends BoardMessage

object BoardMessage {
  implicit val columnMovedFormat: OFormat[ColumnMoved] = Json.format[ColumnMoved]
  implicit val taskMovedFormat: OFormat[TaskMoved]     = Json.format[TaskMoved]

  implicit val writes: Writes[BoardMessage] = Writes {
    case cm: ColumnMoved => Json.obj("type" -> "columnMoved") ++ Json.toJsObject(cm)
    case tm: TaskMoved   => Json.obj("type" -> "taskMoved") ++ Json.toJsObject(tm)
  }
}