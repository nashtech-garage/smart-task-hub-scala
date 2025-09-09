package filters

import play.api.Configuration
import play.api.mvc._
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CorsFilter @Inject()(
    config: Configuration,
    implicit val ec: ExecutionContext
) extends EssentialFilter {

    private val allowedOrigins = config.get[Seq[String]]("security.cors.allowedOrigins")
    private val allowCredentials = config.get[Boolean]("security.cors.allowCredentials")
    private val allowedMethods = config.get[Seq[String]]("security.cors.allowedMethods").mkString(", ")
    private val allowedHeaders = config.get[Seq[String]]("security.cors.allowedHeaders").mkString(", ")
    private val exposedHeaders = config.get[Seq[String]]("security.cors.exposedHeaders").mkString(", ")
    private val maxAge = config.get[Int]("security.cors.maxAge")


    def apply(next: EssentialAction): EssentialAction = EssentialAction { request =>

        // Get origin from request
        val origin = request.headers.get("Origin")

        // Check if origin is allowed
        val allowedOrigin = origin.filter(allowedOrigins.contains)

        if (request.method == "OPTIONS") {
            // Handle preflight requests
            play.api.libs.streams.Accumulator.done(
                Results.Ok
                    .withHeaders(getCorsHeaders(allowedOrigin): _*)
                    .as("text/plain")
            )
        } else {
            // Handle actual requests
            next(request).map { result =>
                allowedOrigin match {
                    case Some(_) => result.withHeaders(getCorsHeaders(allowedOrigin): _*)
                    case None => result
                }
            }
        }
    }

    private def getCorsHeaders(allowedOrigin: Option[String]): List[(String, String)] = {
        val baseHeaders = List(
            "Access-Control-Allow-Methods" -> allowedMethods,
            "Access-Control-Allow-Headers" -> allowedHeaders,
            "Access-Control-Expose-Headers" -> exposedHeaders,
            "Access-Control-Max-Age" -> maxAge.toString
        )

        val originHeader = allowedOrigin.map("Access-Control-Allow-Origin" -> _).toList
        val credentialsHeader = if (allowCredentials) {
            List("Access-Control-Allow-Credentials" -> "true")
        } else {
            List.empty
        }

        baseHeaders ++ originHeader ++ credentialsHeader
    }
}