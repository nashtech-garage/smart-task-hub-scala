package services

import dto.response.auth.{ErrorResponse, TokenResponse}
import play.api.Configuration
import play.api.libs.json.{Json, JsSuccess, JsError}
import play.api.libs.ws._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeycloakService @Inject()(
  ws: StandaloneWSClient,
  config: Configuration
)()(implicit ec: ExecutionContext) {
  private val serverUrl = config.get[String]("keycloak.serverUrl")
  private val realm = config.get[String]("keycloak.realm")
  private val clientId = config.get[String]("keycloak.clientId")
  private val clientSecret = config.get[String]("keycloak.clientSecret")

  private val tokenEndpoint = s"$serverUrl/realms/$realm/protocol/openid-connect/token"

  def login(email: String, password: String): Future[Either[ErrorResponse, TokenResponse]] = {
    val requestBody: Map[String, Seq[String]] = Map(
      "grant_type" -> Seq("password"),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret),
      "username" -> Seq(email),
      "password" -> Seq(password)
    )

    ws.url(tokenEndpoint)
      .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
      .post(requestBody)
      .map { response =>
        response.status match {
          case 200 =>
            Json.parse(response.body[String]).validate[TokenResponse] match {
              case JsSuccess(token, _) => Right(token)
              case JsError(errors) =>
                Left(ErrorResponse("parse_error", Some(s"Failed to parse token: $errors")))
            }
          case _ =>
            Json.parse(response.body[String]).validate[ErrorResponse] match {
              case JsSuccess(error, _) => Left(error)
              case JsError(_) =>
                Left(ErrorResponse("unknown_error", Some(response.body[String])))
            }
        }
      }
      .recover {
        case ex: Exception =>
          Left(ErrorResponse("connection_error", Some(ex.getMessage)))
      }
  }
}
