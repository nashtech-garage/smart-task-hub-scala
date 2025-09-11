package services

import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Flow
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Result, Results}
import repositories.ProjectRepository
import slick.jdbc.JdbcProfile

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/**
  * WebSocket service to manage WebSocket connections.
  */
@Singleton
class WebSocketService @Inject()(
  @Named("projectActorRegistry") projectActorRegistry: ActorRef,
  projectRepository: ProjectRepository,
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit system: ActorSystem, ec: ExecutionContext, mat: Materializer)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  /**
    * Join a project WebSocket if the user is part of the project.
    * @param userId the id of the user trying to join
    * @param projectId the id of the project to join
    * @return a Future containing either a Result (error) or a Flow for the WebSocket connection
    */
  def joinProject(
    userId: Int,
    projectId: Int
  ): Future[Either[Result, Flow[JsValue, JsValue, _]]] = {
    db.run(projectRepository.isUserInProject(userId, projectId)).map {
      case true =>
        Right(ActorFlow.actorRef[JsValue, JsValue] { out =>
          actors.ProjectClientActor
            .props(out, userId, projectId, projectActorRegistry)
        })
      case false =>
        Left(Results.Forbidden("You are not a member of this project"))
    }
  }
}
