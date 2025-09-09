package filters

import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter

import javax.inject.{Inject, Singleton}

@Singleton
class Filters @Inject()(
    corsFilter: CorsFilter,
    securityHeadersFilter: SecurityHeadersFilter,
    apiAccessFilter: ApiAccessFilter,
    gzipFilter: GzipFilter
) extends HttpFilters {

    override val filters = Seq(
        corsFilter,             // Handle CORS first
        securityHeadersFilter, // Add security headers
        apiAccessFilter,      // Authentication & Authorization
        gzipFilter           // Compress responses (built-in Play filter)
    )
}