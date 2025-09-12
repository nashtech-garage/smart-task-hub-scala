package controllers

import modules.ActorNames
import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.ProjectService

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class WebSocketController @Inject()(
                                     cc: ControllerComponents,
                                     authenticatedWebSocket: AuthenticatedWebSocket,
                                     @Named(ActorNames.ProjectActorRegistry) projectActorRegistry: ActorRef,
                                     projectService: ProjectService
                                   )(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext)
  extends AbstractController(cc) {

  def joinProject(projectId: Int): WebSocket = authenticatedWebSocket { userToken =>
    projectService.isUserInActiveProject(userToken.userId, projectId).map { exists =>
      if (exists) {
        Right(
          ActorFlow.actorRef[JsValue, JsValue] { out =>
            actors.ProjectClientActor.props(out, userToken.userId, projectId, projectActorRegistry)
          }
        )
      } else {
        Left(Results.Forbidden("User is not a member of this project"))
      }
    }
  }
}

