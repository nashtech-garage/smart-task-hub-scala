package services

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.util.Timeout
import actors.ProjectActorRegistry
import com.google.inject.name.Named
import dto.websocket.OutgoingMessage
import modules.ActorNames

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.duration._

class BroadcastService @Inject() (@Named(ActorNames.ProjectActorRegistry) registry: ActorRef) {
  import ProjectActorRegistry._

  implicit val timeout: Timeout = Timeout(3.seconds)

  // Broadcast to project actor
  def broadcastToProject(projectId: Int, message: OutgoingMessage): Future[Unit] = {
    registry ! BroadcastToProject(projectId, message)
    Future.successful(())
  }
}
