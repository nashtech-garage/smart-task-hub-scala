package services

import dto.response.user.UserGoogleResponse
import models.entities.User
import play.api.Configuration
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws._
import repositories.RoleRepository
import repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthService @Inject()(
                                   ws: StandaloneWSClient,
                                   config: Configuration,
                                   jwtService: JwtService,
                                   userRepository: UserRepository,
                                   roleRepository: RoleRepository
                              )(implicit ec: ExecutionContext) {
  private val clientId = config.get[String]("google.clientId")
  private val clientSecret = config.get[String]("google.clientSecret")
  private val redirectUri = config.get[String]("google.redirectUri")
  private val tokenUrl = config.get[String]("google.tokenUrl")
  private val userInfoUrl = config.get[String]("google.userInfoUrl")

  def handleGoogleCallback(code: String, state: Option[String]): Future[String] = {
    val requestBody = Map(
      "code" -> Seq(code),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret),
      "redirect_uri" -> Seq(redirectUri),
      "grant_type" -> Seq("authorization_code"),
      "scope" -> Seq("openid email profile")
    )

    ws.url(tokenUrl)
      .post(requestBody)
      .flatMap(
        response => response.status match {
          case 200 =>
            val accessToken = (Json.parse(response.body) \ "access_token").as[String]
            getUserInformationFromGoogle(accessToken)
          case _ =>
            throw new Exception(response.body)
    })
    .recover {
      case ex: Exception => throw ex
    }
  }

  private def getUserInformationFromGoogle(token: String): Future[String] = {
    ws.url(userInfoUrl)
      .addHttpHeaders("Authorization" -> s"Bearer $token")
      .get()
      .flatMap { userResp =>
        Json.parse(userResp.body[String]).validate[UserGoogleResponse] match {
          case JsSuccess(user, _) =>
            createNewUserFromGoogle(user).flatMap { newUser =>
              val userToken = UserToken(newUser.id.get, newUser.name, newUser.email)
              Future.fromTry(jwtService.generateToken(userToken))
            }
          case JsError(errors) => throw new Exception(s"Failure parse to UserGoogleResponse: $errors")
        }
      }.recover {
        case ex: Exception => throw ex
      }
  }

  private def createNewUserFromGoogle(user: UserGoogleResponse): Future[User] = {
    userRepository.findByEmail(user.email).flatMap {
      case Some(existingUser) =>
        Future.successful(existingUser)
      case None =>
        roleRepository.findByRoleName("user").flatMap {
          case Some(role) =>
            val newUser = User(
              name = user.name,
              email = user.email,
              password = "",
              roleId = role.id
            )
            userRepository.create(newUser)
          case None =>
            throw new Exception("Role 'user' not found")
        }
    }
  }
}
