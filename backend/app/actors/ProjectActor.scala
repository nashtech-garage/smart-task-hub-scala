package actors

import dto.websocket.OutgoingMessage
import org.apache.pekko.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

/**
  * Actor that manages WebSocket connections for a specific project.
  */
object ProjectActor {
  def props(projectId: Int): Props = Props(new ProjectActor(projectId))

  case class Join(userId: Int, out: ActorRef)
  case class Leave(userId: Int)
  case class Broadcast(message: OutgoingMessage)
}

/**
  * Actor that manages WebSocket connections for a specific project.
  * It keeps track of connected users and broadcasts messages to them.
  *
  * @param projectId the ID of the project
  */
class ProjectActor(projectId: Int) extends Actor {
  import ProjectActor._

  private var members = Map.empty[Int, ActorRef]

  def receive: Receive = {
    case Join(userId, out) =>
      members += userId -> out
      Logger("actors").info(
        s"UserId $userId joined project $projectId. Total members: ${members.size}"
      )

    case Leave(userId) =>
      members -= userId
      Logger("actors").info(
        s"UserId with $userId left project $projectId. Total members: ${members.size}"
      )

    case Broadcast(msg) =>
      val js: JsValue = Json.toJson(msg)
      members.values.foreach(_ ! js)
      Logger("actors").info(
        s"Broadcasted message to project $projectId members: ${members.size} users"
      )
  }
}
