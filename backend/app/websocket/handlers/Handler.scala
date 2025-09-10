package websocket.handlers

import dto.websocket.InMsg
import org.apache.pekko.stream.scaladsl.SourceQueueWithComplete
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait Handler[M <: InMsg] {
    def handle(msg: M, ctx: HandlerContext): Future[Unit]
}

case class HandlerContext(
    userId: Int,
    outQueue: SourceQueueWithComplete[JsValue]
)
