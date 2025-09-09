package modules

import com.google.inject.AbstractModule
import init.{ApplicationStartup, DatabaseInitializer}

class DatabaseModule extends AbstractModule {

  override def configure(): Unit = {
    // Bind the initialization services
    bind(classOf[DatabaseInitializer]).asEagerSingleton()
    bind(classOf[ApplicationStartup]).asEagerSingleton()
  }

}
