package websocket.codecs

import dto.websocket.{InMsg, OutMsg}
import play.api.libs.json.{Json, OFormat, OWrites}

object ColumnCodecs {
    implicit val moveColumnFormat: OFormat[InMsg.MoveColumn] = Json.format[InMsg.MoveColumn]

    implicit val columnMovedWrites: OWrites[OutMsg.ColumnMoved] =
        Json.writes[OutMsg.ColumnMoved].transform(_ ++ Json.obj("type" -> "columnMoved"))
}
