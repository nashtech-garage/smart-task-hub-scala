package controllers

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.Configuration
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import services.{CookieService, GoogleAuthService}

import scala.concurrent.{ExecutionContext, Future}

class GoogleAuthControllerSpec extends PlaySpec with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private def createController(
                                  mockGoogleAuthService: GoogleAuthService = mock[GoogleAuthService],
                                  mockCookieService: CookieService = mock[CookieService],
                                  frontendUrl: String = "http://localhost:3000"
                              ): (GoogleAuthController, GoogleAuthService, CookieService) = {
    val mockConfig = mock[Configuration]
    when(mockConfig.get[String]("frontend.url")).thenReturn(frontendUrl)

    val controller = new GoogleAuthController(
      stubControllerComponents(),
      mockCookieService,
      mockGoogleAuthService,
      mockConfig
    )
    (controller, mockGoogleAuthService, mockCookieService)
  }

  "GoogleAuthController" should {

    "callback" should {
      "return redirect with auth cookie on successful authentication" in {
        val (controller, mockGoogleAuthService, mockCookieService) = createController()

        val googleCode = "test-google-code-123"
        val state = Some("test-state-456")
        val generatedToken = "jwt.test.token"
        val mockCookie = play.api.mvc.Cookie("auth_token", generatedToken)

        when(mockGoogleAuthService.handleGoogleCallback(googleCode, state))
          .thenReturn(Future.successful(generatedToken))
        when(mockCookieService.createAuthCookie(generatedToken))
          .thenReturn(mockCookie)

        val fakeRequest = FakeRequest(GET, s"/auth/google/callback?code=$googleCode&state=test-state-456")

        val result: Future[Result] = controller.callback(googleCode, state)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("http://localhost:3000")
        verify(mockGoogleAuthService, times(1)).handleGoogleCallback(googleCode, state)
        verify(mockCookieService, times(1)).createAuthCookie(generatedToken)
      }

      "call handleGoogleCallback with correct parameters" in {
        val (controller, mockGoogleAuthService, mockCookieService) = createController()

        val googleCode = "auth-code-xyz"
        val state = Some("state-abc")
        val token = "jwt.token.here"
        val mockCookie = play.api.mvc.Cookie("auth", token)

        when(mockGoogleAuthService.handleGoogleCallback(googleCode, state))
          .thenReturn(Future.successful(token))
        when(mockCookieService.createAuthCookie(token))
          .thenReturn(mockCookie)

        val fakeRequest = FakeRequest(GET, "/auth/google/callback")

        controller.callback(googleCode, state)(fakeRequest)

        verify(mockGoogleAuthService).handleGoogleCallback(googleCode, state)
      }

      "handle callback without state parameter" in {
        val (controller, mockGoogleAuthService, mockCookieService) = createController()

        val googleCode = "code-without-state"
        val state = None
        val token = "jwt.no.state"
        val mockCookie = play.api.mvc.Cookie("token", token)

        when(mockGoogleAuthService.handleGoogleCallback(googleCode, state))
          .thenReturn(Future.successful(token))
        when(mockCookieService.createAuthCookie(token))
          .thenReturn(mockCookie)

        val fakeRequest = FakeRequest(GET, "/auth/google/callback?code=code-without-state")

        val result: Future[Result] = controller.callback(googleCode, state)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("http://localhost:3000")
      }

      "handle different frontend URLs from configuration" in {
        val prodFrontendUrl = "https://app.example.com"
        val (controller, mockGoogleAuthService, mockCookieService) = createController(frontendUrl = prodFrontendUrl)

        val googleCode = "code123"
        val state = Some("state123")
        val token = "jwt.token"
        val mockCookie = play.api.mvc.Cookie("auth", token)

        when(mockGoogleAuthService.handleGoogleCallback(googleCode, state))
          .thenReturn(Future.successful(token))
        when(mockCookieService.createAuthCookie(token))
          .thenReturn(mockCookie)

        val fakeRequest = FakeRequest(GET, "/auth/google/callback")

        val result: Future[Result] = controller.callback(googleCode, state)(fakeRequest)

        redirectLocation(result) mustBe Some(prodFrontendUrl)
      }

      "create auth cookie with token from service" in {
        val (controller, mockGoogleAuthService, mockCookieService) = createController()

        val googleCode = "code"
        val state = Some("state")
        val expectedToken = "jwt.expected.token.value"
        val mockCookie = play.api.mvc.Cookie("authToken", expectedToken)

        when(mockGoogleAuthService.handleGoogleCallback(googleCode, state))
          .thenReturn(Future.successful(expectedToken))
        when(mockCookieService.createAuthCookie(expectedToken))
          .thenReturn(mockCookie)

        val fakeRequest = FakeRequest(GET, "/auth/google/callback")

        val result: Future[Result] = controller.callback(googleCode, state)(fakeRequest)

        status(result) mustBe SEE_OTHER
        verify(mockCookieService).createAuthCookie(expectedToken)
      }

      "handle empty code parameter" in {
        val (controller, mockGoogleAuthService, mockCookieService) = createController()

        val googleCode = ""
        val state = Some("state")
        val token = "jwt.token"
        val mockCookie = play.api.mvc.Cookie("auth", token)

        when(mockGoogleAuthService.handleGoogleCallback(googleCode, state))
          .thenReturn(Future.successful(token))
        when(mockCookieService.createAuthCookie(token))
          .thenReturn(mockCookie)

        val fakeRequest = FakeRequest(GET, "/auth/google/callback?code=&state=state")

        val result: Future[Result] = controller.callback(googleCode, state)(fakeRequest)

        status(result) mustBe SEE_OTHER
      }

      "handle multiple callback invocations independently" in {
        val (controller, mockGoogleAuthService, mockCookieService) = createController()

        val code1 = "code-1"
        val code2 = "code-2"
        val state1 = Some("state-1")
        val state2 = Some("state-2")
        val token1 = "jwt.token.1"
        val token2 = "jwt.token.2"
        val mockCookie1 = play.api.mvc.Cookie("auth", token1)
        val mockCookie2 = play.api.mvc.Cookie("auth", token2)

        when(mockGoogleAuthService.handleGoogleCallback(code1, state1))
          .thenReturn(Future.successful(token1))
        when(mockGoogleAuthService.handleGoogleCallback(code2, state2))
          .thenReturn(Future.successful(token2))
        when(mockCookieService.createAuthCookie(token1))
          .thenReturn(mockCookie1)
        when(mockCookieService.createAuthCookie(token2))
          .thenReturn(mockCookie2)

        val fakeRequest1 = FakeRequest(GET, "/auth/google/callback?code=code-1&state=state-1")
        val fakeRequest2 = FakeRequest(GET, "/auth/google/callback?code=code-2&state=state-2")

        val result1: Future[Result] = controller.callback(code1, state1)(fakeRequest1)
        val result2: Future[Result] = controller.callback(code2, state2)(fakeRequest2)

        status(result1) mustBe SEE_OTHER
        status(result2) mustBe SEE_OTHER
        redirectLocation(result1) mustBe Some("http://localhost:3000")
        redirectLocation(result2) mustBe Some("http://localhost:3000")
      }
    }
  }
}

