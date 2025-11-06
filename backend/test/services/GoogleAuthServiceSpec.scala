package services

import dto.response.user.UserGoogleResponse
import models.entities.{Role, User}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws._
import repositories.{RoleRepository, UserRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class GoogleAuthServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val patience: PatienceConfig = PatienceConfig(5.seconds, 100.millis)
  implicit val ec: ExecutionContext = ExecutionContext.global

  // Mock dependencies
  val mockWs: StandaloneWSClient = mock[StandaloneWSClient]
  val mockConfig: Configuration = mock[Configuration]
  val mockJwtService: JwtService = mock[JwtService]
  val mockUserRepository: UserRepository = mock[UserRepository]
  val mockRoleRepository: RoleRepository = mock[RoleRepository]

  // Configuration values
  val clientId = "test-client-id"
  val clientSecret = "test-client-secret"
  val redirectUri = "http://localhost:9000/callback"
  val tokenUrl = "https://oauth2.googleapis.com/token"
  val userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo"

  // Setup configuration mocks
  when(mockConfig.get[String]("google.clientId")).thenReturn(clientId)
  when(mockConfig.get[String]("google.clientSecret")).thenReturn(clientSecret)
  when(mockConfig.get[String]("google.redirectUri")).thenReturn(redirectUri)
  when(mockConfig.get[String]("google.tokenUrl")).thenReturn(tokenUrl)
  when(mockConfig.get[String]("google.userInfoUrl")).thenReturn(userInfoUrl)

  val service = new GoogleAuthService(
    mockWs,
    mockConfig,
    mockJwtService,
    mockUserRepository,
    mockRoleRepository
  )

  "GoogleAuthService" should {

    "handleGoogleCallback" should {

      "successfully handle callback and return JWT token for existing user" in {
        // Arrange
        val code = "test-auth-code"
        val accessToken = "test-access-token"
        val jwtToken = "test-jwt-token"

        val tokenResponse = Json.obj(
          "access_token" -> accessToken,
          "token_type" -> "Bearer",
          "expires_in" -> 3600
        )

        val userInfo = UserGoogleResponse(
          name = "Test User",
          email = "test@example.com",
          given_name = "Test",
          family_name = "User",
          sub = "google-sub-id",
          email_verified = true
        )

        val existingUser = User(
          id = Some(1),
          name = "Test User",
          email = "test@example.com",
          password = "",
          roleId = Some(1)
        )

        // Create proper mock hierarchy
        val mockTokenRequest = mock[StandaloneWSRequest {
          type Response = StandaloneWSResponse
        }]
        val mockTokenResponse = mock[StandaloneWSResponse]
        val mockUserInfoRequest = mock[StandaloneWSRequest {
          type Self = StandaloneWSRequest { type Response = StandaloneWSResponse }
          type Response = StandaloneWSResponse
        }]
        val mockUserInfoResponse = mock[StandaloneWSResponse]

        // Mock token exchange
        when(mockWs.url(tokenUrl)).thenReturn(mockTokenRequest)
        when(mockTokenRequest.post(any[Map[String, Seq[String]]]())(any())).thenReturn(Future.successful(mockTokenResponse))
        when(mockTokenResponse.status).thenReturn(200)
        when(mockTokenResponse.body).thenReturn(tokenResponse.toString())

        // Mock user info request
        when(mockWs.url(userInfoUrl)).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.addHttpHeaders(any().asInstanceOf[(String, String)]))
          .thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.get()).thenReturn(Future.successful(mockUserInfoResponse))
        when(mockUserInfoResponse.body[String]).thenReturn(Json.toJson(userInfo).toString())

        // Mock user repository
        when(mockUserRepository.findByEmail(userInfo.email)).thenReturn(Future.successful(Some(existingUser)))

        // Mock JWT service
        val userToken = UserToken(1, "Test User", "test@example.com")
        when(mockJwtService.generateToken(userToken)).thenReturn(Success(jwtToken))

        // Act & Assert
        service.handleGoogleCallback(code, None).map { result =>
          result shouldBe jwtToken

          // Verify interactions
          verify(mockUserRepository).findByEmail(userInfo.email)
          verify(mockJwtService).generateToken(userToken)
          verify(mockUserRepository, never()).create(any[User])
          assert(true)
        }
      }

      "successfully handle callback and create new user" in {
        // Arrange
        val code = "test-auth-code"
        val accessToken = "test-access-token"
        val jwtToken = "test-jwt-token"

        val tokenResponse = Json.obj(
          "access_token" -> accessToken,
          "token_type" -> "Bearer",
          "expires_in" -> 3600
        )

        val userInfo = UserGoogleResponse(
          name = "Test User",
          email = "newuser@example.com",
          given_name = "Test",
          family_name = "User",
          sub = "google-sub-id",
          email_verified = true
        )

        val role = Role(
          id = Some(2),
          name = "user"
        )

        val newUser = User(
          id = Some(2),
          name = "New User",
          email = "newuser@example.com",
          password = "",
          roleId = Some(2)
        )

        // Create proper mock hierarchy
        val mockTokenRequest = mock[StandaloneWSRequest {
          type Response = StandaloneWSResponse
        }]
        val mockTokenResponse = mock[StandaloneWSResponse]
        val mockUserInfoRequest = mock[StandaloneWSRequest {
          type Self = StandaloneWSRequest { type Response = StandaloneWSResponse }
          type Response = StandaloneWSResponse
        }]
        val mockUserInfoResponse = mock[StandaloneWSResponse]

        // Mock token exchange
        when(mockWs.url(tokenUrl)).thenReturn(mockTokenRequest)
        when(mockTokenRequest.post(any[Map[String, Seq[String]]]())(any())).thenReturn(Future.successful(mockTokenResponse))
        when(mockTokenResponse.status).thenReturn(200)
        when(mockTokenResponse.body).thenReturn(tokenResponse.toString())

        // Mock user info request
        when(mockWs.url(userInfoUrl)).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.addHttpHeaders(any[(String, String)])).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.get()).thenReturn(Future.successful(mockUserInfoResponse))
        when(mockUserInfoResponse.body[String]).thenReturn(Json.toJson(userInfo).toString())

        // Mock repositories
        when(mockUserRepository.findByEmail(userInfo.email)).thenReturn(Future.successful(None))
        when(mockRoleRepository.findByRoleName("user")).thenReturn(Future.successful(Some(role)))
        when(mockUserRepository.create(any[User])).thenReturn(Future.successful(newUser))

        // Mock JWT service
        val userToken = UserToken(2, "New User", "newuser@example.com")
        when(mockJwtService.generateToken(userToken)).thenReturn(Success(jwtToken))

        // Act & Assert
        service.handleGoogleCallback(code, None).map { result =>
          result shouldBe jwtToken

          // Verify interactions
          verify(mockUserRepository).findByEmail(userInfo.email)
          verify(mockRoleRepository).findByRoleName("user")
          verify(mockUserRepository).create(any[User])
          verify(mockJwtService).generateToken(userToken)
          assert(true)
        }
      }

      "throw exception when token exchange fails" in {
        // Arrange
        val code = "test-auth-code"
        val errorMessage = "Invalid authorization code"

        val mockTokenRequest = mock[StandaloneWSRequest {
          type Response = StandaloneWSResponse
        }]
        val mockTokenResponse = mock[StandaloneWSResponse]
        val mockUserInfoRequest = mock[StandaloneWSRequest {
          type Self = StandaloneWSRequest { type Response = StandaloneWSResponse }
          type Response = StandaloneWSResponse
        }]
        val mockUserInfoResponse = mock[StandaloneWSResponse]

        when(mockWs.url(tokenUrl)).thenReturn(mockTokenRequest)
        when(mockTokenRequest.post(any[Map[String, Seq[String]]]())(any())).thenReturn(Future.successful(mockTokenResponse))
        when(mockTokenResponse.status).thenReturn(400)
        when(mockTokenResponse.body).thenReturn(errorMessage)

        // Act & Assert
        val exception = intercept[Exception] {
          service.handleGoogleCallback(code, None).futureValue
        }
        exception.getMessage should include(errorMessage)
      }

      "throw exception when user info parsing fails" in {
        // Arrange
        val code = "test-auth-code"
        val accessToken = "test-access-token"

        val tokenResponse = Json.obj(
          "access_token" -> accessToken,
          "token_type" -> "Bearer",
          "expires_in" -> 3600
        )

        val invalidUserInfo = Json.obj(
          "invalid" -> "data"
        )

        val mockTokenRequest = mock[StandaloneWSRequest {
          type Response = StandaloneWSResponse
        }]
        val mockTokenResponse = mock[StandaloneWSResponse]
        val mockUserInfoRequest = mock[StandaloneWSRequest {
          type Self = StandaloneWSRequest { type Response = StandaloneWSResponse }
          type Response = StandaloneWSResponse
        }]
        val mockUserInfoResponse = mock[StandaloneWSResponse]

        // Mock token exchange
        when(mockWs.url(tokenUrl)).thenReturn(mockTokenRequest)
        when(mockTokenRequest.post(any[Map[String, Seq[String]]]())(any())).thenReturn(Future.successful(mockTokenResponse))
        when(mockTokenResponse.status).thenReturn(200)
        when(mockTokenResponse.body).thenReturn(tokenResponse.toString())

        // Mock user info request
        when(mockWs.url(userInfoUrl)).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.addHttpHeaders(any[(String, String)])).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.get()).thenReturn(Future.successful(mockUserInfoResponse))
        when(mockUserInfoResponse.body[String]).thenReturn(invalidUserInfo.toString())

        // Act & Assert
        val exception = intercept[Exception] {
          service.handleGoogleCallback(code, None).futureValue
        }
        exception.getMessage should include("Failure parse to UserGoogleResponse")
      }

      "throw exception when role 'user' is not found" in {
        // Arrange
        val code = "test-auth-code"
        val accessToken = "test-access-token"

        val tokenResponse = Json.obj(
          "access_token" -> accessToken,
          "token_type" -> "Bearer",
          "expires_in" -> 3600
        )

        val userInfo = UserGoogleResponse(
          name = "Test User",
          email = "new@example.com",
          given_name = "Test",
          family_name = "User",
          sub = "google-sub-id",
          email_verified = true
        )

        val mockTokenRequest = mock[StandaloneWSRequest {
          type Response = StandaloneWSResponse
        }]
        val mockTokenResponse = mock[StandaloneWSResponse]
        val mockUserInfoRequest = mock[StandaloneWSRequest {
          type Self = StandaloneWSRequest { type Response = StandaloneWSResponse }
          type Response = StandaloneWSResponse
        }]
        val mockUserInfoResponse = mock[StandaloneWSResponse]

        // Mock token exchange
        when(mockWs.url(tokenUrl)).thenReturn(mockTokenRequest)
        when(mockTokenRequest.post(any[Map[String, Seq[String]]]())(any())).thenReturn(Future.successful(mockTokenResponse))
        when(mockTokenResponse.status).thenReturn(200)
        when(mockTokenResponse.body).thenReturn(tokenResponse.toString())

        // Mock user info request
        when(mockWs.url(userInfoUrl)).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.addHttpHeaders(any[(String, String)])).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.get()).thenReturn(Future.successful(mockUserInfoResponse))
        when(mockUserInfoResponse.body[String]).thenReturn(Json.toJson(userInfo).toString())

        // Mock repositories
        when(mockUserRepository.findByEmail(userInfo.email)).thenReturn(Future.successful(None))
        when(mockRoleRepository.findByRoleName("user")).thenReturn(Future.successful(None))

        // Act & Assert
        val exception = intercept[Exception] {
          service.handleGoogleCallback(code, None).futureValue
        }
        exception.getMessage should include("Role 'user' not found")
      }

      "throw exception when JWT generation fails" in {
        // Arrange
        val code = "test-auth-code"
        val accessToken = "test-access-token"

        val tokenResponse = Json.obj(
          "access_token" -> accessToken,
          "token_type" -> "Bearer",
          "expires_in" -> 3600
        )

        val userInfo = UserGoogleResponse(
          name = "Test User",
          email = "test@example.com",
          given_name = "Test",
          family_name = "User",
          sub = "google-sub-id",
          email_verified = true
        )

        val existingUser = User(
          id = Some(1),
          name = "Test User",
          email = "test@example.com",
          password = "",
          roleId = Some(1)
        )

        val mockTokenRequest = mock[StandaloneWSRequest {
          type Response = StandaloneWSResponse
        }]
        val mockTokenResponse = mock[StandaloneWSResponse]
        val mockUserInfoRequest = mock[StandaloneWSRequest {
          type Self = StandaloneWSRequest { type Response = StandaloneWSResponse }
          type Response = StandaloneWSResponse
        }]
        val mockUserInfoResponse = mock[StandaloneWSResponse]

        // Mock token exchange
        when(mockWs.url(tokenUrl)).thenReturn(mockTokenRequest)
        when(mockTokenRequest.post(any[Map[String, Seq[String]]]())(any())).thenReturn(Future.successful(mockTokenResponse))
        when(mockTokenResponse.status).thenReturn(200)
        when(mockTokenResponse.body).thenReturn(tokenResponse.toString())

        // Mock user info request
        when(mockWs.url(userInfoUrl)).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.addHttpHeaders(any[(String, String)])).thenReturn(mockUserInfoRequest)
        when(mockUserInfoRequest.get()).thenReturn(Future.successful(mockUserInfoResponse))
        when(mockUserInfoResponse.body[String]).thenReturn(Json.toJson(userInfo).toString())

        // Mock user repository
        when(mockUserRepository.findByEmail(userInfo.email)).thenReturn(Future.successful(Some(existingUser)))

        // Mock JWT service failure
        val userToken = UserToken(1, "Test User", "test@example.com")
        when(mockJwtService.generateToken(userToken)).thenReturn(Failure(new Exception("JWT generation failed")))

        // Act & Assert
        val exception = intercept[Exception] {
          service.handleGoogleCallback(code, None).futureValue
        }
        exception.getMessage should include("JWT generation failed")
      }
    }
  }
}
