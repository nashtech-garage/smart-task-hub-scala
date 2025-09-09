package services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.play.PlaySpec
import play.api.Configuration

import java.util.Date
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class JwtServiceSpec extends PlaySpec {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val mockConfig = Configuration(
    "jwt.secret" -> "test-secret-key",
    "jwt.expiration.hours" -> 24,
    "jwt.issuer" -> "test-issuer",
    "cookie.name" -> "authToken",
    "cookie.maxAge" -> 3600,
    "cookie.secure" -> false,
    "cookie.httpOnly" -> true,
    "cookie.sameSite" -> "Strict"
  )

  val service = new JwtService(mockConfig)

  val testUserToken = UserToken(
    userId = 123,
    name = "John Doe",
    email = "john.doe@example.com"
  )

  "JwtService" should {
    "initialize with correct configuration" in {
      service.cookieConfig.name mustBe "authToken"
      service.cookieConfig.maxAge mustBe 3600
      service.cookieConfig.secure mustBe false
      service.cookieConfig.httpOnly mustBe true
      service.cookieConfig.sameSite mustBe "Strict"
    }
  }

  "JwtService#generateToken" should {
    "return Success with valid token for valid user" in {
      val result = service.generateToken(testUserToken)

      result mustBe a[Success[_]]
      result.get must not be empty

      // Verify token structure
      val token = result.get
      val decodedJWT = JWT.decode(token)

      decodedJWT.getSubject mustBe testUserToken.userId.toString
      decodedJWT.getClaim("name").asString() mustBe testUserToken.name
      decodedJWT.getClaim("email").asString() mustBe testUserToken.email
      decodedJWT.getIssuer mustBe "test-issuer"
    }
    "generate token with correct expiration time" in {
      val result = service.generateToken(testUserToken)
      result mustBe a[Success[_]]

      val token = result.get
      val decodedJWT = JWT.decode(token)
      val expirationTime = decodedJWT.getExpiresAt.getTime
      val issuedAtTime = decodedJWT.getIssuedAt.getTime

      // Check that expiration is 24 hours after issued time
      val expectedExpiration = issuedAtTime + (24.hours.toMillis)
      expirationTime mustBe expectedExpiration
    }
  }

  "JwtService#validateToken" should {
    "return Success with UserToken for valid token" in {
      val tokenResult = service.generateToken(testUserToken)
      tokenResult mustBe a[Success[_]]

      val validationResult = service.validateToken(tokenResult.get)

      validationResult mustBe a[Success[_]]
      val userToken = validationResult.get

      userToken.userId mustBe testUserToken.userId
      userToken.name mustBe testUserToken.name
      userToken.email mustBe testUserToken.email
    }

    "return Failure for invalid token format" in {
      val result = service.validateToken("invalid-token")

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Token verification failed")
    }

    "return Failure for empty token" in {
      val result = service.validateToken("")

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Token verification failed")
    }

    "return Failure for token with wrong signature" in {
      val wrongSecret = "wrong-secret-key-for-jwt-testing-purposes-only"
      val wrongAlgorithm = Algorithm.HMAC256(wrongSecret)

      val maliciousToken = JWT.create()
        .withSubject(testUserToken.userId.toString)
        .withClaim("name", testUserToken.name)
        .withClaim("email", testUserToken.email)
        .withIssuer("test-issuer")
        .sign(wrongAlgorithm)

      val result = service.validateToken(maliciousToken)

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Token verification failed")
    }

    "return Failure for token with wrong issuer" in {
      val testSecret = "test-secret-key"
      val algorithm = Algorithm.HMAC256(testSecret)

      val tokenWithWrongIssuer = JWT.create()
        .withSubject(testUserToken.userId.toString)
        .withClaim("name", testUserToken.name)
        .withClaim("email", testUserToken.email)
        .withIssuer("wrong-issuer")
        .sign(algorithm)

      val result = service.validateToken(tokenWithWrongIssuer)

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Token verification failed")
    }

    "return Failure for expired token" in {
      val testSecret = "test-secret-key"
      val algorithm = Algorithm.HMAC256(testSecret)
      val pastDate = new Date(System.currentTimeMillis() - 1000) // 1 second ago

      val expiredToken = JWT.create()
        .withSubject(testUserToken.userId.toString)
        .withClaim("name", testUserToken.name)
        .withClaim("email", testUserToken.email)
        .withIssuer("test-issuer")
        .withExpiresAt(pastDate)
        .sign(algorithm)

      val result = service.validateToken(expiredToken)

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Token verification failed")
    }

    "return Failure for token missing email claim" in {
      val testSecret = "test-secret-key"
      val algorithm = Algorithm.HMAC256(testSecret)

      val tokenWithoutEmail = JWT.create()
        .withSubject(testUserToken.userId.toString)
        .withClaim("name", testUserToken.name)
        .withIssuer("test-issuer")
        .sign(algorithm)

      val result = service.validateToken(tokenWithoutEmail)

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Token validation error: Invalid token: missing email")
    }

    "return Failure for token missing name claim" in {
      val testSecret = "test-secret-key"
      val algorithm = Algorithm.HMAC256(testSecret)

      val tokenWithoutName = JWT.create()
        .withSubject(testUserToken.userId.toString)
        .withClaim("email", testUserToken.email)
        .withIssuer("test-issuer")
        .sign(algorithm)

      val result = service.validateToken(tokenWithoutName)

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Invalid token: missing name")
    }

    "return Failure for token with empty subject" in {
      val testSecret = "test-secret-key"
      val algorithm = Algorithm.HMAC256(testSecret)

      val tokenWithEmptySubject = JWT.create()
        .withSubject("")
        .withClaim("name", testUserToken.name)
        .withClaim("email", testUserToken.email)
        .withIssuer("test-issuer")
        .sign(algorithm)

      val result = service.validateToken(tokenWithEmptySubject)

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Invalid token: missing user ID")
    }

    "return Failure for token with null subject" in {
      val testSecret = "test-secret-key"
      val algorithm = Algorithm.HMAC256(testSecret)

      val tokenWithNullSubject = JWT.create()
        .withClaim("name", testUserToken.name)
        .withClaim("email", testUserToken.email)
        .withIssuer("test-issuer")
        .sign(algorithm)

      val result = service.validateToken(tokenWithNullSubject)

      result mustBe a[Failure[_]]
      result.failed.get.getMessage must include("Invalid token: missing user ID")
    }
  }

  "JwtService#isTokenValid" should {
    "return true for valid token" in {
      val tokenResult = service.generateToken(testUserToken)
      tokenResult mustBe a[Success[_]]

      val isValid = service.isTokenValid(tokenResult.get)

      isValid mustBe true
    }

    "return false for invalid token" in {
      val isValid = service.isTokenValid("invalid-token")

      isValid mustBe false
    }

    "return false for empty token" in {
      val isValid = service.isTokenValid("")

      isValid mustBe false
    }

    "return false for expired token" in {
      val testSecret = "test-secret-key"
      val algorithm = Algorithm.HMAC256(testSecret)
      val pastDate = new Date(System.currentTimeMillis() - 1000)

      val expiredToken = JWT.create()
        .withSubject(testUserToken.userId.toString)
        .withClaim("name", testUserToken.name)
        .withClaim("email", testUserToken.email)
        .withIssuer("test-issuer")
        .withExpiresAt(pastDate)
        .sign(algorithm)

      val isValid = service.isTokenValid(expiredToken)

      isValid mustBe false
    }
  }

  "JwtService#refreshToken" should {
    "return Success with new token for valid user" in {
      val originalTokenResult = service.generateToken(testUserToken)
      originalTokenResult mustBe a[Success[_]]

      // Wait a bit to ensure different issued at time
      Thread.sleep(1000)

      val refreshedTokenResult = service.refreshToken(testUserToken)

      refreshedTokenResult mustBe a[Success[_]]
      refreshedTokenResult.get must not be empty
      refreshedTokenResult.get must not equal originalTokenResult.get

      // Verify refreshed token is valid
      val validationResult = service.validateToken(refreshedTokenResult.get)
      validationResult mustBe a[Success[_]]

      val userToken = validationResult.get
      userToken.userId mustBe testUserToken.userId
      userToken.name mustBe testUserToken.name
      userToken.email mustBe testUserToken.email
    }

    "generate different tokens on multiple refresh calls" in {
      val firstRefresh = service.refreshToken(testUserToken)
      Thread.sleep(1000)
      val secondRefresh = service.refreshToken(testUserToken)

      firstRefresh mustBe a[Success[_]]
      secondRefresh mustBe a[Success[_]]
      firstRefresh.get must not equal secondRefresh.get
    }
  }

  "JwtService integration scenarios" should {
    "support complete token lifecycle" in {
      // Generate token
      val generateResult = service.generateToken(testUserToken)
      generateResult mustBe a[Success[_]]

      val token = generateResult.get

      // Validate token
      val validateResult = service.validateToken(token)
      validateResult mustBe a[Success[_]]

      // Check validity
      service.isTokenValid(token) mustBe true

      // Refresh token
      val refreshResult = service.refreshToken(testUserToken)
      refreshResult mustBe a[Success[_]]

      // Validate refreshed token
      val validateRefreshedResult = service.validateToken(refreshResult.get)
      validateRefreshedResult mustBe a[Success[_]]

      // Both tokens should be valid
      service.isTokenValid(token) mustBe true
      service.isTokenValid(refreshResult.get) mustBe true
    }

    "handle user with different data correctly" in {
      val anotherUser = UserToken(456, "Jane Smith", "jane@example.com")

      val tokenResult = service.generateToken(anotherUser)
      tokenResult mustBe a[Success[_]]

      val validationResult = service.validateToken(tokenResult.get)
      validationResult mustBe a[Success[_]]

      val retrievedUser = validationResult.get
      retrievedUser.userId mustBe 456
      retrievedUser.name mustBe "Jane Smith"
      retrievedUser.email mustBe "jane@example.com"
    }
  }


}
