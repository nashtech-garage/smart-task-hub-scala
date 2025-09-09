package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import play.api.http.HttpFilters
import filters.Filters

class SecurityModule(environment: Environment, configuration: Configuration)
    extends AbstractModule {

    override def configure(): Unit = {
        // Bind your custom filters.Filters impl as the HttpFilters Play should use:
        bind(classOf[HttpFilters]).to(classOf[Filters])
    }
}

