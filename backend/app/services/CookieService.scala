package services

import play.api.mvc.{Cookie, Request, RequestHeader, Result}
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class CookieService @Inject()(config: Configuration) {

    private val cookieName = config.get[String]("cookie.name")
    private val maxAge = config.get[Int]("cookie.maxAge")
    private val secure = config.get[Boolean]("cookie.secure")
    private val httpOnly = config.get[Boolean]("cookie.httpOnly")
    private val sameSiteStr = config.get[String]("cookie.sameSite")

    private val sameSite = sameSiteStr.toLowerCase match {
        case "strict" => Some(Cookie.SameSite.Strict)
        case "lax" => Some(Cookie.SameSite.Lax)
        case "none" => Some(Cookie.SameSite.None)
        case _ => Some(Cookie.SameSite.Strict)
    }

    def createAuthCookie(token: String): Cookie = {
        Cookie(
            name = cookieName,
            value = token,
            maxAge = Some(maxAge),
            path = "/",
            domain = None, // Will use current domain
            secure = secure,
            httpOnly = httpOnly,
            sameSite = sameSite
        )
    }

    def createExpiredAuthCookie(): Cookie = {
        Cookie(
            name = cookieName,
            value = "",
            maxAge = Some(0), // Expire immediately
            path = "/",
            domain = None,
            secure = secure,
            httpOnly = httpOnly,
            sameSite = sameSite
        )
    }

    def getTokenFromRequest(request: RequestHeader): Option[String] = {
        request.cookies.get(cookieName).map(_.value).filter(_.nonEmpty)
    }

    def addTokenCookie(result: Result, token: String): Result = {
        result.withCookies(createAuthCookie(token))
    }

    def removeTokenCookie(result: Result): Result = {
        result.withCookies(createExpiredAuthCookie())
    }
}