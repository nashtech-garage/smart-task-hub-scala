package modules

import actors.ProjectActorRegistry
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import org.apache.pekko.actor.{ActorRef, ActorSystem, Props}
import play.api.Logger

/**
 * Constants for actor names to ensure consistency across the application.
 */
object ActorNames {
  final val ProjectActorRegistry = "project-actor-registry"
  final val ProjectActorPrefix = "project-actor-"
}

/**
 * Module to provide actor instances.
 */
class ActorsModule extends AbstractModule {
  @Provides
  @Singleton
  @Named(ActorNames.ProjectActorRegistry)
  def provideProjectActorRegistry(system: ActorSystem): ActorRef = {
    Logger("actors").info("Creating ProjectActorRegistry actor")
    system.actorOf(Props[ProjectActorRegistry], ActorNames.ProjectActorRegistry)
  }
}
