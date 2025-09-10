package websocket.handlers

import com.google.inject.Inject
import websocket.codecs.ColumnCodecs._
import websocket.codecs.DomainCodecs._
import dto.request.column.UpdateColumnPositionRequest
import dto.websocket.InMsg.{ColumnInMsg, MoveColumn}
import dto.websocket.OutMsg
import play.api.libs.json.Json
import services.ColumnService

import scala.concurrent.{ExecutionContext, Future}

class ColumnHandler @Inject() (columnService: ColumnService)(implicit ec: ExecutionContext)
    extends Handler[ColumnInMsg] {

    override def handle(msg: ColumnInMsg, ctx: HandlerContext): Future[Unit] = msg match {
        case MoveColumn(boardId, columnId, newPos) =>
            columnService
                .updatePosition(columnId, UpdateColumnPositionRequest(newPos), ctx.userId)
                .map { _ =>
                    ctx.outQueue.offer(Json.toJson(OutMsg.ColumnMoved(boardId, columnId, newPos)))
                    ()
                }
                .recover { case ex =>
                    ctx.outQueue.offer(Json.toJson(OutMsg.ErrorMsg(ex.getMessage)))
                }
    }
}
