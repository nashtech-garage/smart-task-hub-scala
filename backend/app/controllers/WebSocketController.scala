package controllers

import dto.websocket.{InMsg, OutMsg}
import dto.websocket.InMsg.{ColumnInMsg, Join, Ping}
import dto.websocket.OutMsg.{ErrorMsg, Joined}
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.{KillSwitches, Materializer, OverflowStrategy, UniqueKillSwitch}
import org.apache.pekko.stream.scaladsl.{Flow, Keep, Sink, Source}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{InjectedController, WebSocket}
import services.Rooms
import websocket.handlers.{ColumnHandler, HandlerContext}
import websocket.codecs.DomainCodecs._
import websocket.codecs.WebSocketCodecs._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WebSocketController @Inject()(
                                       rooms: Rooms,
                                       columnHandler: ColumnHandler,
                                       authenticatedWebSocket: AuthenticatedWebSocket
                                )(implicit mat: Materializer, ec: ExecutionContext) extends InjectedController {

    def ws: WebSocket = authenticatedWebSocket { userToken ⇒
        socketFlow(userToken.userId)
    }

    private def socketFlow(userId: Int): Flow[JsValue, JsValue, NotUsed] = {
        val (outQueue, outSource) =
            Source.queue[JsValue](64, OverflowStrategy.dropHead).preMaterialize()

        var currentKillSwitch: Option[UniqueKillSwitch] = None

        val inbound: Sink[JsValue, NotUsed] =
            Flow[JsValue].mapAsync(1) {
                case js if js.validate[InMsg].isSuccess =>
                    js.as[InMsg] match {
                        case Ping =>
                            outQueue.offer(Json.toJson(OutMsg.Pong))

                        case Join(boardId) =>
                            // unsubscribe if already joined another board
                            currentKillSwitch.foreach(_.shutdown())
                            val (_, roomSrc) = rooms.room(boardId)

                            val (ks, _) = roomSrc
                                .viaMat(KillSwitches.single)(Keep.right)
                                .map(Json.parse)
                                .toMat(Sink.foreach(outQueue.offer))(Keep.both)
                                .run()

                            currentKillSwitch = Some(ks)
                            outQueue.offer(Json.toJson(Joined(boardId, userId))).map(_ => ())

                        case msg: ColumnInMsg =>
                            columnHandler.handle(msg, HandlerContext(userId, outQueue))

                        case _ =>
                            outQueue.offer(Json.toJson(ErrorMsg("Unknown message"))).map(_ => ())
                    }

                case other =>
                    Future.successful(
                        outQueue.offer(Json.toJson(ErrorMsg(s"Invalid JSON: $other")))
                    ).map(_ => ())
            }.to(Sink.ignore)

        Flow.fromSinkAndSourceCoupled(inbound, outSource)
    }
}
