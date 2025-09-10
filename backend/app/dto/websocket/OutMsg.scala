package dto.websocket

sealed trait OutMsg
object OutMsg {

    case object Pong extends OutMsg
    case class Joined(boardId: Int, userId: Int) extends OutMsg
    case class ErrorMsg(error: String) extends OutMsg

    // column
    case class ColumnMoved(boardId: Int, columnId: Int, newPos: Int) extends OutMsg
}
