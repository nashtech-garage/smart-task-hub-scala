package actors

import org.apache.pekko.actor.{Actor, ActorRef, Props}
import org.apache.pekko.pattern.{ask, pipe}
import org.apache.pekko.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Actor that manages a WebSocket connection for a user in a specific project.
 */
object ProjectClientActor {
  def props(out: ActorRef,
            userId: Int,
            projectId: Int,
            registry: ActorRef): Props =
    Props(new ProjectClientActor(out, userId, projectId, registry))

  /**
   * Message indicating that the ProjectActor has been found in the registry.
   * @param projectRef the ActorRef of the ProjectActor
   */
  private case class RegistryFound(projectRef: ActorRef)
}

/**
 * Actor that manages a WebSocket connection for a user in a specific project.
 * It registers the user with the ProjectActor upon creation and deregisters
 * upon termination.
 *
 * @param out      the ActorRef to send messages to the WebSocket
 * @param userId   the ID of the user
 * @param projectId the ID of the project
 * @param registry the ActorRef of the ProjectActorRegistry
 */
class ProjectClientActor(out: ActorRef,
                         userId: Int,
                         projectId: Int,
                         registry: ActorRef)
    extends Actor {
  import ProjectActor._
  import ProjectActorRegistry._
  import ProjectClientActor._

  // Execution context and timeout for ask pattern
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = Timeout(3.seconds)

  // Reference to the ProjectActor, once found
  private var projectRefOpt: Option[ActorRef] = None

  // On start, ask the registry for the ProjectActor
  override def preStart(): Unit = {
    (registry ? GetProjectActor(projectId))
      .mapTo[ActorRef]
      .map(RegistryFound) pipeTo self
  }

  // On stop, inform the ProjectActor that the user is leaving
  override def postStop(): Unit = {
    projectRefOpt.foreach(_ ! Leave(userId))
    super.postStop()
  }

  // Handle incoming messages
  def receive: Receive = {
    case RegistryFound(projectRef) =>
      projectRefOpt = Some(projectRef)
      projectRef ! Join(userId, out)
  }
}
