package filters

import play.api.Configuration
import play.api.mvc._
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SecurityHeadersFilter @Inject()(
    config: Configuration,
    implicit val ec: ExecutionContext
) extends EssentialFilter {

    private val csp = config.get[String]("security.headers.contentSecurityPolicy")
    private val frameOptions = config.get[String]("security.headers.frameOptions")
    private val contentTypeOptions = config.get[String]("security.headers.contentTypeOptions")
    private val referrerPolicy = config.get[String]("security.headers.referrerPolicy")
    private val permissionsPolicy = config.get[String]("security.headers.permissionsPolicy")

    def apply(next: EssentialAction): EssentialAction = EssentialAction { request ⇒
        next(request).map { result =>
            result.withHeaders(
                "Content-Security-Policy" -> csp,
                "X-Frame-Options" -> frameOptions,
                "X-Content-Type-Options" -> contentTypeOptions,
                "Referrer-Policy" -> referrerPolicy,
                "Permissions-Policy" -> permissionsPolicy,
                "X-XSS-Protection" -> "1; mode=block",
                "Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"
            )
        }
    }
}