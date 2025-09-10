package services

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.{Materializer, OverflowStrategy}
import org.apache.pekko.stream.scaladsl.{BroadcastHub, Keep, Source, SourceQueueWithComplete}

import javax.inject.{Inject, Singleton}

@Singleton
class Rooms @Inject()(implicit mat: Materializer) {

    private var rooms: Map[Int, (SourceQueueWithComplete[String], Source[String, NotUsed])] = Map.empty

    def room(boardId: Int): (SourceQueueWithComplete[String], Source[String, NotUsed]) =
        rooms.getOrElse(boardId, {
            // Each board has its own queue + broadcast hub
            val (queue, src) = Source
                .queue[String](bufferSize = 128, overflowStrategy = OverflowStrategy.dropHead)
                .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
                .run()

            val room = (queue, src)
            rooms += boardId -> room
            room
        })
}
