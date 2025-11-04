package actors

import org.apache.pekko.actor.{Actor, ActorRef, Props}
import dto.websocket.OutgoingMessage
import modules.ActorNames

object ProjectActorRegistry {
  def props: Props = Props(new ProjectActorRegistry)

  case class GetProjectActor(projectId: Int)
  case class BroadcastToProject(projectId: Int, message: OutgoingMessage)
}

class ProjectActorRegistry extends Actor {
  import ProjectActorRegistry._
  import ProjectActor._

  private var projectActors = Map.empty[Int, ActorRef]

  def receive: Receive = {
    case GetProjectActor(projectId) =>
      // create new ProjectActor if not exists
      val actor = projectActors.getOrElse(projectId, {
        val newActor = context.actorOf(ProjectActor.props(projectId), s"${ActorNames.ProjectActorPrefix}$projectId")
        projectActors += projectId -> newActor
        newActor
      })
      // return ActorRef for requester
      sender() ! actor

    case BroadcastToProject(projectId, message) =>
      projectActors.get(projectId).foreach(_ ! Broadcast(message))
  }
}
