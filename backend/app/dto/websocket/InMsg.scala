package dto.websocket

sealed trait InMsg
object InMsg {

    case object Ping extends InMsg
    case class Join(boardId: Int) extends InMsg

    // Column domain
    sealed trait ColumnInMsg extends InMsg
    case class MoveColumn(boardId: Int, columnId: Int, newPos: Int) extends ColumnInMsg
}
