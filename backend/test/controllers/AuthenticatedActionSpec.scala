package controllers

import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import services.{CookieService, JwtService, UserToken}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthenticatedActionSpec extends PlaySpec with MockitoSugar with Results {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val playBodyParsers: PlayBodyParsers = stubControllerComponents().parsers
  val bodyParsers: BodyParsers.Default = new BodyParsers.Default(playBodyParsers)

  "AuthenticatedAction" should {

    "return Unauthorized when token is missing" in {
      val cookieService = mock[CookieService]
      val jwtService = mock[JwtService]

      when(cookieService.getTokenFromRequest(any())).thenReturn(None)

      val action = new AuthenticatedAction(bodyParsers, jwtService, cookieService)

      val result = action.invokeBlock(FakeRequest(), (req: Request[AnyContent]) => Future.successful(Ok("OK")))
      status(result) mustBe UNAUTHORIZED
      contentAsString(result) must include("No authentication token found")
    }

    "return Unauthorized when token is invalid" in {
      val cookieService = mock[CookieService]
      val jwtService = mock[JwtService]

      val token = "invalid.token"
      val expiredCookie = DiscardingCookie("authToken")

      when(cookieService.getTokenFromRequest(any())).thenReturn(Some(token))
      when(jwtService.validateToken(token)).thenReturn(Failure(new Exception("Invalid signature")))
      when(cookieService.createExpiredAuthCookie()).thenReturn(expiredCookie.toCookie)

      val action = new AuthenticatedAction(bodyParsers, jwtService, cookieService)

      val result = action.invokeBlock(FakeRequest(), (req: Request[AnyContent]) => Future.successful(Ok("OK")))
      status(result) mustBe UNAUTHORIZED
      contentAsString(result) must include("Invalid token: Invalid signature")
    }

    "call block when token is valid" in {
      val cookieService = mock[CookieService]
      val jwtService = mock[JwtService]

      val token = "valid.token"

      when(cookieService.getTokenFromRequest(any())).thenReturn(Some(token))
      when(jwtService.validateToken(token)).thenReturn(Success(mock[UserToken]))

      val action = new AuthenticatedAction(bodyParsers, jwtService, cookieService)

      val result = action.invokeBlock(FakeRequest(), (req: Request[AnyContent]) => Future.successful(Ok("OK")))
      status(result) mustBe OK
      contentAsString(result) mustBe "OK"
    }
  }
}
