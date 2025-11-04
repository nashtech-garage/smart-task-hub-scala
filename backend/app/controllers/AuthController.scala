package controllers

import dto.request.auth.{LoginRequest, RegisterUserRequest}
import dto.response.ApiResponse
import dto.response.auth.AuthResponse
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import services._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Controller responsible for handling authentication-related HTTP requests.
 *
 * @param cc          Controller components for Play framework.
 * @param authService Service for authentication operations.
 * @param ec          Execution context for asynchronous operations.
 */
@Singleton
class AuthController @Inject()(
                                keycloakService: KeycloakService,
                                authService: AuthService,
                                cc: ControllerComponents,
                                jwtService: JwtService,
                                cookieService: CookieService,
                                roleService: RoleService,
                                authenticatedActionWithUser: AuthenticatedActionWithUser
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def register(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    parseRegisterRequest(request.body).fold(
      error ⇒ Future.successful(BadRequest(Json.toJson(ApiResponse.error[AuthResponse](error)))),
      registerRequest ⇒ authService.registerUser(registerRequest).map { user =>
        val responseData = AuthResponse(
          user.id.get,
          user.name,
          user.email
        )
        Created(Json.toJson(ApiResponse.success("User registered successfully", responseData)))
      }
    )
  }

  def login(): Action[AnyContent] = Action.async { implicit request ⇒
    parseLoginRequest(request.body).fold(
      error ⇒ Future.successful(BadRequest(Json.toJson(ApiResponse.error[AuthResponse](error)))),
      loginReq ⇒ handleKeycloakLogin(loginReq)
    )
  }

  def logout(): Action[AnyContent] = Action { implicit request ⇒
    val apiResponse = ApiResponse.success[String]("Logged out successfully")
    Ok(Json.toJson(apiResponse))
      .withCookies(cookieService.createExpiredAuthCookie())
  }

  def me(): Action[AnyContent] = authenticatedActionWithUser { implicit request =>
    val authResponse = authService.userTokenToAuthResponse(request.userToken)
    val apiResponse = ApiResponse.success("User information retrieved", authResponse)
    Ok(Json.toJson(apiResponse))
  }

  def refresh(): Action[AnyContent] = authenticatedActionWithUser { implicit request =>
    jwtService.refreshToken(request.userToken) match {
      case Success(newToken) =>
        val authResponse = authService.userTokenToAuthResponse(request.userToken)
        val apiResponse = ApiResponse.success[AuthResponse]("Token refreshed successfully", authResponse)
        Ok(Json.toJson(apiResponse))
          .withCookies(cookieService.createAuthCookie(newToken))
      case Failure(ex) =>
        val apiResponse = ApiResponse.error[AuthResponse](s"Token refresh failed: ${ex.getMessage}")
        InternalServerError(Json.toJson(apiResponse))
          .withCookies(cookieService.createExpiredAuthCookie())
    }
  }

  def checkAuth(): Action[AnyContent] = Action.async { implicit request =>
    withAuthenticatedUserAsync(request) { userToken ⇒
      val authResponse = authService.userTokenToAuthResponse(userToken)
      val apiResponse = ApiResponse.success("User is authenticated", authResponse)
      Future.successful(Ok(Json.toJson(apiResponse)))
    }
  }

  def roleRetrieve(): Action[AnyContent] = Action.async { implicit request ⇒
    withAuthenticatedUserAsync(request) { userToken ⇒
      roleService.getUserRole(userToken.userId).map {
        case Some(role) ⇒
          val apiResponse = ApiResponse.success("User is authenticated", role)
          Ok(Json.toJson(apiResponse))

        case None ⇒
          val apiResponse = ApiResponse.error[String]("No role found")
          NotFound(Json.toJson(apiResponse))
      }
    }
  }

  private def withAuthenticatedUserAsync[A](request: Request[A])
                                           (block: UserToken ⇒ Future[Result]): Future[Result] = {
    cookieService.getTokenFromRequest(request) match {
      case Some(token) ⇒
        jwtService.validateToken(token) match {
          case Success(userToken) ⇒ block(userToken)
          case Failure(_) ⇒
            val apiResponse = ApiResponse.error[AuthResponse]("Invalid or expired token")
            Future.successful(Unauthorized(Json.toJson(apiResponse)))
        }
      case None ⇒
        val apiResponse = ApiResponse.error[AuthResponse]("No authentication token found")
        Future.successful(Unauthorized(Json.toJson(apiResponse)))
    }
  }

  private def parseLoginRequest(body: AnyContent): Either[String, LoginRequest] = {
    body.asJson match {
      case Some(json) ⇒
        json.validate[LoginRequest] match {
          case JsSuccess(loginReq, _) ⇒ Right(loginReq)
          case JsError(_) ⇒ Left("Invalid request format. Please provide email and password")
        }
      case None ⇒ Left("JSON body required")
    }
  }

  private def handleLogin(loginReq: LoginRequest): Future[Result] = {
    authService.authenticateUser(loginReq.email, loginReq.password).flatMap {
      case Some(userToken) ⇒
        jwtService.generateToken(userToken) match {
          case Success(token) ⇒
            val authResponse = authService.userTokenToAuthResponse(userToken)
            val apiResponse = ApiResponse.success("Login successful", authResponse)
            Future.successful(Ok(Json.toJson(apiResponse)).withCookies(cookieService.createAuthCookie(token)))

          case Failure(exception) ⇒
            val apiResponse = ApiResponse.error[AuthResponse](s"Token generation failed: ${exception.getMessage}")
            Future.successful(InternalServerError(Json.toJson(apiResponse)))
        }

      case None ⇒
        val apiResponse = ApiResponse.error[AuthResponse]("Invalid email or password")
        Future.successful(Unauthorized(Json.toJson(apiResponse)))
    }
  }

  private def handleKeycloakLogin(loginReq: LoginRequest): Future[Result] = {
    keycloakService.login(loginReq.email, loginReq.password).flatMap {
      case Right(tokenResponse) =>
        Future.successful(Ok(Json.toJson(tokenResponse)).withCookies(cookieService.createAuthCookie(tokenResponse.access_token)))
      case Left(error) =>
        Future.successful(InternalServerError(error))
    }
  }

  private def parseRegisterRequest(body: JsValue): Either[String, RegisterUserRequest] = {
    body.validate[RegisterUserRequest] match {
      case JsSuccess(loginReq, _) ⇒ Right(loginReq)
      case JsError(validationErrors) ⇒
        val errors = validationErrors.flatMap { case (path, errs) =>
          errs.map { err =>
            val field = path.toString().replaceAll("[\\[\\]/]", "")
            val message = err.message match {
                case "error.minLength" => s"$field is too short"
                case "error.maxLength" => s"$field is too long"
                case "error.email" => s"$field is not valid"
                case "error.path.missing" => s"$field is required"
                case other => s"$field: $other"
            }
            message
          }
      }.toList
        Left(errors.head)
    }
  }
}