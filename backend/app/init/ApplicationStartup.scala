package init

import play.api.{Logger, Logging}
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class ApplicationStartup @Inject()(
                                    lifecycle: ApplicationLifecycle,
                                    databaseInitializer: DatabaseInitializer
                                  )(implicit ec: ExecutionContext) {

  // This will run when the application starts
  initialize()



  private def initialize(): Unit = {
    Logger("application").info("Application startup initialization...")
    try {
      Await.result(databaseInitializer.initializeDatabase(), 10.seconds)
    } catch {
      case ex: Throwable =>
        Logger("application").error("Failed to initialize database", ex)
    }
  }


}