package controllers

import play.api.Configuration
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext
import services.{CookieService, GoogleAuthService}

@Singleton
class GoogleAuthController @Inject()(
                                      cc: ControllerComponents,
                                      cookieService: CookieService,
                                      googleAuthService: GoogleAuthService,
                                      config: Configuration
                                    )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val frontEndUrl = config.get[String]("frontend.url")

  def callback(code: String, state: Option[String]):Action[AnyContent] = Action.async { implicit request =>
    googleAuthService.handleGoogleCallback(code, state).map { token =>
      Redirect(frontEndUrl).withCookies(cookieService.createAuthCookie(token))
    }
  }
}