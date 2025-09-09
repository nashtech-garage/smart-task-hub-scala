package controllers

import dto.request.auth.RegisterUserRequest
import dto.response.auth.AuthResponse
import models.entities.User
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{Cookie, Request, Result}
import play.api.test.Helpers._
import play.api.test._
import services.{AuthService, CookieService, JwtService, RoleService, UserToken}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthControllerSpec extends PlaySpec with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private def createController(
                                  mockService: AuthService = mock[AuthService]
                              ): (AuthController, AuthService) = {
    val mockJwtService = mock[JwtService]
    val mockCookieService = mock[CookieService]
    val roleService = mock[RoleService]
    val mockAuthenticatedActionWithUser = mock[AuthenticatedActionWithUser]

    val controller = new AuthController(
      mockService,
      stubControllerComponents(),
      mockJwtService,
      mockCookieService,
      roleService,
      mockAuthenticatedActionWithUser
    )
    (controller, mockService)
  }

  "register" should {
    "return 201 Created with valid input" in {
      val (controller, mockService) = createController()
      val requestJson = Json.obj(
        "name" -> "John",
        "email" -> "john@example.com",
        "password" -> "abc123"
      )

      val fakeRequest = FakeRequest(POST, "/register")
          .withBody(requestJson)
          .withHeaders("Content-Type" -> "application/json")

      when(mockService.registerUser(any[RegisterUserRequest]))
          .thenReturn(Future.successful(
            User(Some(1), "John", "john@example.com", "hashed", None, Some(1), LocalDateTime.now(), LocalDateTime.now())
          ))

      val result = controller.register()(fakeRequest)

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String].toLowerCase must include("registered")
    }

    "return 400 BadRequest when input is invalid (missing name)" in {
      val (controller, _) = createController()
      val invalidJson = Json.obj(
        "email" -> "invalid@example.com",
        "password" -> "123"
      )

      val fakeRequest = FakeRequest(POST, "/register")
          .withBody(invalidJson)
          .withHeaders("Content-Type" -> "application/json")

      val result = controller.register()(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result).toLowerCase must include("name")
    }
  }

  "login" should {
    "return 200 OK when credentials are valid" in {
      val mockService = mock[AuthService]
      val mockJwtService = mock[JwtService]
      val mockCookieService = mock[CookieService]

      val userToken = mock[UserToken]
      val token = "jwt.token"

      when(mockService.authenticateUser("john@example.com", "abc123"))
          .thenReturn(Future.successful(Some(userToken)))
      when(mockJwtService.generateToken(userToken)).thenReturn(Success(token))
      when(mockService.userTokenToAuthResponse(userToken)).thenReturn(AuthResponse(1, "John", "john@example.com"))
      when(mockCookieService.createAuthCookie(token)).thenReturn(Cookie("authToken", token))

      val controller = new AuthController(mockService, stubControllerComponents(), mockJwtService,
        mockCookieService, mock[RoleService], mock[AuthenticatedActionWithUser])

      val requestJson = Json.obj("email" -> "john@example.com", "password" -> "abc123")
      val request = FakeRequest(POST, "/login").withJsonBody(requestJson)

      val result = controller.login()(request)
      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String].toLowerCase must include("login")
    }

    "return 401 Unauthorized for invalid credentials" in {
      val (controller, mockService) = createController()
      when(mockService.authenticateUser(any[String], any[String])).thenReturn(Future.successful(None))

      val requestJson = Json.obj("email" -> "a@a.com", "password" -> "wrong")
      val request = FakeRequest(POST, "/login").withJsonBody(requestJson)

      val result = controller.login()(request)
      status(result) mustBe UNAUTHORIZED
    }

    "return 400 BadRequest if request is not JSON" in {
      val controller = createController()._1
      val request = FakeRequest(POST, "/login")

      val result = controller.login()(request)
      status(result) mustBe BAD_REQUEST
    }
  }

  "logout" should {
    "return 200 OK and clear cookie" in {
      val mockCookieService = mock[CookieService]
      when(mockCookieService.createExpiredAuthCookie()).thenReturn(Cookie("authToken", "", maxAge = Some(0)))

      val controller = new AuthController(mock[AuthService], stubControllerComponents(), mock[JwtService],
        mockCookieService, mock[RoleService], mock[AuthenticatedActionWithUser])

      val result = controller.logout()(FakeRequest(POST, "/logout"))
      status(result) mustBe OK
      cookies(result).get("authToken").exists(_.value.isEmpty) mustBe true
    }
  }

  "checkAuth" should {
    "return 200 OK if user is authenticated" in {
      val mockAuthService = mock[AuthService]
      val mockJwtService = mock[JwtService]
      val mockCookieService = mock[CookieService]
      val mockUserToken = mock[UserToken]

      val token = "valid.jwt.token"
      val fakeRequest = FakeRequest(GET, "/check-auth")
          .withCookies(Cookie("authToken", token))

      when(mockCookieService.getTokenFromRequest(fakeRequest)).thenReturn(Some(token))
      when(mockJwtService.validateToken(token)).thenReturn(Success(mockUserToken))
      when(mockAuthService.userTokenToAuthResponse(mockUserToken)).thenReturn(AuthResponse(1, "John", "john@example.com"))

      val controller = new AuthController(
        mockAuthService,
        stubControllerComponents(),
        mockJwtService,
        mockCookieService,
        mock[RoleService],
        mock[AuthenticatedActionWithUser]
      )

      val result = controller.checkAuth()(fakeRequest)
      status(result) mustBe OK
    }

    "return 401 Unauthorized if token is missing" in {
      val mockCookieService = mock[CookieService]
      when(mockCookieService.getTokenFromRequest(any())).thenReturn(None)

      val controller = new AuthController(
        mock[AuthService],
        stubControllerComponents(),
        mock[JwtService],
        mockCookieService,
        mock[RoleService],
        mock[AuthenticatedActionWithUser]
      )

      val result = controller.checkAuth()(FakeRequest())
      status(result) mustBe UNAUTHORIZED
    }

    "return 401 Unauthorized if token is invalid" in {
      val mockCookieService = mock[CookieService]
      val mockJwtService = mock[JwtService]
      val token = "invalid.token"

      when(mockCookieService.getTokenFromRequest(any())).thenReturn(Some(token))
      when(mockJwtService.validateToken(token)).thenReturn(Failure(new RuntimeException("Invalid token")))

      val controller = new AuthController(
        mock[AuthService],
        stubControllerComponents(),
        mockJwtService,
        mockCookieService,
        mock[RoleService],
        mock[AuthenticatedActionWithUser]
      )

      val result = controller.checkAuth()(FakeRequest().withCookies(Cookie("authToken", token)))
      status(result) mustBe UNAUTHORIZED
    }
  }

  "roleRetrieve" should {
    "return 200 OK with role when user is authenticated and has role" in {
      val mockCookieService = mock[CookieService]
      val mockJwtService = mock[JwtService]
      val mockRoleService = mock[RoleService]

      val userToken = UserToken(1, "John", "john@example.com")
      val token = "valid.jwt.token"
      val fakeRequest = FakeRequest(GET, "/role")
          .withCookies(Cookie("authToken", token))

      when(mockCookieService.getTokenFromRequest(fakeRequest)).thenReturn(Some(token))
      when(mockJwtService.validateToken(token)).thenReturn(Success(userToken))
      when(mockRoleService.getUserRole(1)).thenReturn(Future.successful(Some("admin")))

      val controller = new AuthController(
        mock[AuthService],
        stubControllerComponents(),
        mockJwtService,
        mockCookieService,
        mockRoleService,
        mock[AuthenticatedActionWithUser]
      )

      val result = controller.roleRetrieve()(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) must include("admin")
    }

    "return 404 NotFound when user has no role" in {
      val mockCookieService = mock[CookieService]
      val mockJwtService = mock[JwtService]
      val mockRoleService = mock[RoleService]
      val userToken = UserToken(1, "John", "john@example.com")
      val token = "valid.jwt.token"
      val fakeRequest = FakeRequest(GET, "/role")
          .withCookies(Cookie("authToken", token))

      when(mockCookieService.getTokenFromRequest(fakeRequest)).thenReturn(Some(token))
      when(mockJwtService.validateToken(token)).thenReturn(Success(userToken))
      when(mockRoleService.getUserRole(1)).thenReturn(Future.successful(None))

      val controller = new AuthController(
        mock[AuthService],
        stubControllerComponents(),
        mockJwtService,
        mockCookieService,
        mockRoleService,
        mock[AuthenticatedActionWithUser]
      )

      val result = controller.roleRetrieve()(fakeRequest)
      status(result) mustBe NOT_FOUND
    }
  }

  "me" should {
    "return 200 OK with user info" in {
      val mockAuthService = mock[AuthService]
      val mockUserToken = UserToken(1, "John", "john@example.com")
      val expectedResponse = AuthResponse(1, "John", "john@example.com")

      when(mockAuthService.userTokenToAuthResponse(mockUserToken)).thenReturn(expectedResponse)

      val mockAction = new AuthenticatedActionWithUser(null, null, null) {
        override def invokeBlock[A](
                                       request: Request[A],
                                       block: AuthenticatedRequest[A] => Future[Result]
                                   ): Future[Result] = {
          val authReq = AuthenticatedRequest(mockUserToken, request)
          block(authReq)
        }
      }

      val controller = new AuthController(
        mockAuthService,
        stubControllerComponents(),
        mock[JwtService],
        mock[CookieService],
        mock[RoleService],
        mockAction
      )

      val result = controller.me()(FakeRequest(GET, "/me"))
      status(result) mustBe OK
      contentAsString(result) must include("John")
      contentAsString(result) must include("john@example.com")
    }
  }

  "refresh" should {
    "return 200 OK with refreshed token and set new auth cookie" in {
      val mockAuthService = mock[AuthService]
      val mockJwtService = mock[JwtService]
      val mockCookieService = mock[CookieService]
      val userToken = UserToken(1, "John", "john@example.com")
      val newToken = "new.jwt.token"
      val authResponse = AuthResponse(1, "John", "john@example.com")
      val authCookie = Cookie("authToken", newToken)

      when(mockJwtService.refreshToken(userToken)).thenReturn(Success(newToken))
      when(mockAuthService.userTokenToAuthResponse(userToken)).thenReturn(authResponse)
      when(mockCookieService.createAuthCookie(newToken)).thenReturn(authCookie)

      val mockAction = new AuthenticatedActionWithUser(null, null, null) {
        override def invokeBlock[A](
                                       request: Request[A],
                                       block: AuthenticatedRequest[A] => Future[Result]
                                   ): Future[Result] = {
          block(AuthenticatedRequest(userToken, request))
        }
      }

      val controller = new AuthController(
        mockAuthService,
        stubControllerComponents(),
        mockJwtService,
        mockCookieService,
        mock[RoleService],
        mockAction
      )

      val result = controller.refresh()(FakeRequest(GET, "/refresh"))
      status(result) mustBe OK
      cookies(result).get("authToken").map(_.value) mustBe Some(newToken)
    }

    "return 500 InternalServerError and expired cookie if token refresh fails" in {
      val mockAuthService = mock[AuthService]
      val mockJwtService = mock[JwtService]
      val mockCookieService = mock[CookieService]
      val userToken = UserToken(1, "John", "john@example.com")
      val expiredCookie = Cookie("authToken", "", maxAge = Some(0))

      when(mockJwtService.refreshToken(userToken))
          .thenReturn(Failure(new RuntimeException("Token expired")))
      when(mockCookieService.createExpiredAuthCookie()).thenReturn(expiredCookie)

      val mockAction = new AuthenticatedActionWithUser(null, null, null) {
        override def invokeBlock[A](
                                       request: Request[A],
                                       block: AuthenticatedRequest[A] => Future[Result]
                                   ): Future[Result] = {
          block(AuthenticatedRequest(userToken, request))
        }
      }

      val controller = new AuthController(
        mockAuthService,
        stubControllerComponents(),
        mockJwtService,
        mockCookieService,
        mock[RoleService],
        mockAction
      )

      val result = controller.refresh()(FakeRequest(GET, "/refresh"))
      status(result) mustBe INTERNAL_SERVER_ERROR
      cookies(result).get("authToken").map(_.value) mustBe Some("")
    }
  }

}
