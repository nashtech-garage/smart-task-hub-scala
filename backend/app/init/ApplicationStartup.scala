package init

import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class ApplicationStartup @Inject()(
                                    databaseInitializer: DatabaseInitializer
                                  )(implicit ec: ExecutionContext) {

  // This will run when the application starts
  initialize()


  private def initialize(): Unit = {
    Logger("application").info("Application startup initialization...")
    try {
      Await.result(databaseInitializer.initializeDatabase(), Duration.Inf)
      Logger("application").info("Application startup initialization completed.")
    } catch {
      case ex: Throwable =>
        Logger("application").error("Failed to initialize database", ex)
    }
  }

}