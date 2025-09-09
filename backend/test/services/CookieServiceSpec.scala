package services

import org.scalatestplus.play._
import play.api.Configuration
import play.api.mvc._
import play.api.test._
import scala.concurrent.ExecutionContext

class CookieServiceSpec extends PlaySpec {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    val mockConfig = Configuration(
        "cookie.name" -> "authToken",
        "cookie.maxAge" -> 3600,
        "cookie.secure" -> false,
        "cookie.httpOnly" -> true,
        "cookie.sameSite" -> "Strict"
    )

    val service = new CookieService(mockConfig)
    val cookieDecoder = new DefaultCookieHeaderEncoding()

    "CookieService#createAuthCookie" should {
        "return a correctly configured cookie" in {
            val token = "sample-token"
            val cookie = service.createAuthCookie(token)

            cookie.name mustBe "authToken"
            cookie.value mustBe token
            cookie.maxAge mustBe Some(3600)
            cookie.secure mustBe false
            cookie.httpOnly mustBe true
            cookie.sameSite mustBe Some(Cookie.SameSite.Strict)
            cookie.path mustBe "/"
        }
    }

    "CookieService#createExpiredAuthCookie" should {
        "return a cookie with maxAge 0 and empty value" in {
            val cookie = service.createExpiredAuthCookie()

            cookie.name mustBe "authToken"
            cookie.value mustBe ""
            cookie.maxAge mustBe Some(0)
        }
    }

    "CookieService#getTokenFromRequest" should {
        "return token if cookie is present and non-empty" in {
            val request = FakeRequest().withCookies(Cookie("authToken", "valid-token"))
            val tokenOpt = service.getTokenFromRequest(request)

            tokenOpt mustBe Some("valid-token")
        }

        "return None if cookie is not present" in {
            val request = FakeRequest()
            val tokenOpt = service.getTokenFromRequest(request)

            tokenOpt mustBe None
        }

        "return None if cookie is present but empty" in {
            val request = FakeRequest().withCookies(Cookie("authToken", ""))
            val tokenOpt = service.getTokenFromRequest(request)

            tokenOpt mustBe None
        }
    }

    "CookieService#addTokenCookie" should {
        "attach auth cookie to the result" in {
            val token = "add-token"
            val result = Results.Ok("response")
            val updatedResult = service.addTokenCookie(result, token)

            val cookies = cookiesHeader(updatedResult)
            cookies.exists(_.name == "authToken") mustBe true
            cookies.find(_.name == "authToken").get.value mustBe token
        }
    }

    "CookieService#removeTokenCookie" should {
        "attach expired cookie to the result" in {
            val result = Results.Ok("response")
            val updatedResult = service.removeTokenCookie(result)

            val cookies = cookiesHeader(updatedResult)
            cookies.exists(_.name == "authToken") mustBe true
            val expired = cookies.find(_.name == "authToken").get
            expired.value mustBe ""
            expired.maxAge mustBe Some(0)
        }
    }

    // Helper to extract cookies from Result
    private def cookiesHeader(result: Result): Seq[Cookie] = {
        result.newCookies
    }
}
