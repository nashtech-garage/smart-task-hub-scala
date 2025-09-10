package websocket.codecs

import play.api.libs.json._
import DomainCodecs._
import ColumnCodecs._
import dto.websocket.{InMsg, OutMsg}

object WebSocketCodecs {
    // InMsg: read "type" then delegate to domain Reads
    implicit val inMsgReads: Reads[InMsg] = Reads { js =>
        (js \ "type").validate[String].flatMap {
            case t if t.equalsIgnoreCase("ping") =>
                JsSuccess(InMsg.Ping)

            case t if t.equalsIgnoreCase("join") =>
                js.validate[InMsg.Join]

            case t if t.equalsIgnoreCase("columnMoved") || t.equalsIgnoreCase("moveColumn") =>
                // normalize naming: frontend might send "moveColumn" or "MoveColumn"
                js.validate[InMsg.MoveColumn]

            // add more cases for other domains:
            // case t if t.equalsIgnoreCase("moveTask") => js.validate[InMsg.MoveTask]

            case other =>
                JsError(s"Unknown type: $other")
        }
    }

    // OutMsg: choose correct domain writer (these writers are imported from domain codec objects)
    implicit val outMsgWrites: Writes[OutMsg] = Writes {
        case OutMsg.Pong             => Json.toJson(OutMsg.Pong)(pongWrites)
        case j: OutMsg.Joined        => Json.toJson(j)(joinedWrites)
        case c: OutMsg.ColumnMoved   => Json.toJson(c)(columnMovedWrites)
        case e: OutMsg.ErrorMsg      => Json.toJson(e)(errorWrites)
        // add Task/Comment branches here later
    }
}

