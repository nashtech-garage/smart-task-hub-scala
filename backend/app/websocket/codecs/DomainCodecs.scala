package websocket.codecs

import dto.websocket.{InMsg, OutMsg}
import play.api.libs.json.{Json, OFormat, OWrites}

object DomainCodecs {
    // Join read/write
    implicit val joinFormat: OFormat[InMsg.Join] = Json.format[InMsg.Join]

    // Out writes for simple system messages
    implicit val pongWrites: OWrites[OutMsg.Pong.type] =
        OWrites(_ => Json.obj("type" -> "pong"))

    implicit val joinedWrites: OWrites[OutMsg.Joined] =
        Json.writes[OutMsg.Joined].transform(_ ++ Json.obj("type" -> "joined"))

    implicit val errorWrites: OWrites[OutMsg.ErrorMsg] =
        Json.writes[OutMsg.ErrorMsg].transform(_ ++ Json.obj("type" -> "error"))
}
