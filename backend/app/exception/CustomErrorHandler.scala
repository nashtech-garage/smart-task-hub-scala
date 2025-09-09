package exception

import dto.response.ApiResponse
import play.api.{Configuration, Logger}
import play.api.http.{HttpErrorHandler, Status}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}

import javax.inject.Inject
import scala.concurrent.Future

class CustomErrorHandler@Inject()(
                                   config: Configuration
                                 ) extends HttpErrorHandler {

  private val allowedOrigins = config.get[Seq[String]]("security.cors.allowedOrigins").mkString(", ")
  private val allowCredentials = config.get[Boolean]("security.cors.allowCredentials")
  private val allowedMethods = config.get[Seq[String]]("security.cors.allowedMethods").mkString(", ")
  private val allowedHeaders = config.get[Seq[String]]("security.cors.allowedHeaders").mkString(", ")

  private def withCORS(result: Result): Result = {
    result.withHeaders(
      "Access-Control-Allow-Origin" -> allowedOrigins,
      "Access-Control-Allow-Headers" -> allowedHeaders,
      "Access-Control-Allow-Credentials" -> allowCredentials.toString,
      "Access-Control-Allow-Methods" -> allowedMethods
    )
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val customMessage = statusCode match {
      case Status.BAD_REQUEST => "Bad request"
      case Status.UNAUTHORIZED => "Unauthorized"
      case Status.FORBIDDEN => "Forbidden"
      case Status.NOT_FOUND => "Resource not found"
      case Status.METHOD_NOT_ALLOWED => "Method not allowed"
      case _ => message
    }

    Future.successful(withCORS(
      Results.Status(statusCode)(
        Json.toJson(
          ApiResponse.errorNoData(customMessage)
        )
      ))
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger("application").error("Server Error", exception)

    exception match {
      case appEx: AppException =>
        Future.successful(withCORS(Results.Status(appEx.statusCode)(
          Json.toJson(
            ApiResponse.error[String](appEx.message)
          )
        )))

      case _ =>
        Future.successful(withCORS(Results.InternalServerError(
          Json.toJson(
            ApiResponse.errorNoData(exception.getMessage)
          )
        )))
    }

  }
}
