package controllers

import org.apache.pekko.stream.scaladsl.Flow
import play.api.libs.json.JsValue
import play.api.mvc._
import services.{CookieService, JwtService, UserToken}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class AuthenticatedRequest[A](userToken: UserToken, request: Request[A])
    extends WrappedRequest[A](request)

class AuthenticatedAction @Inject()(
    parser: BodyParsers.Default,
    jwtService: JwtService,
    cookieService: CookieService
)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {

    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
        cookieService.getTokenFromRequest(request) match {
            case Some(token) =>
                jwtService.validateToken(token) match {
                    case Success(_) => block(request)
                    case Failure(ex) =>
                        Future.successful(
                            Results.Unauthorized(s"Invalid token: ${ex.getMessage}")
                                .withCookies(cookieService.createExpiredAuthCookie())
                        )
                }
            case None =>
                Future.successful(Results.Unauthorized("No authentication token found"))
        }
    }
}

class AuthenticatedActionWithUser @Inject()(
   parser: BodyParsers.Default,
   jwtService: JwtService,
   cookieService: CookieService
)(implicit ec: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] {
    override def parser: BodyParser[AnyContent] = parser
    override protected def executionContext: ExecutionContext = ec

    override def invokeBlock[A](
                                   request: Request[A],
                                   block: AuthenticatedRequest[A] => Future[Result]
                               ): Future[Result] = {
        cookieService.getTokenFromRequest(request) match {
            case Some(token) =>
                jwtService.validateToken(token) match {
                    case Success(userToken) =>
                        val authenticatedRequest = AuthenticatedRequest(userToken, request)
                        block(authenticatedRequest)
                    case Failure(ex) =>
                        Future.successful(
                            Results.Unauthorized(s"Invalid token: ${ex.getMessage}")
                                .withCookies(cookieService.createExpiredAuthCookie())
                        )
                }
            case None =>
                Future.successful(Results.Unauthorized("No authentication token found"))
        }
    }
}

class AuthenticatedWebSocket @Inject()(
                                          jwtService: JwtService,
                                          cookieService: CookieService
                                      )(implicit ec: ExecutionContext) {

  def apply(
    block: UserToken ⇒ Future[Either[Result, Flow[JsValue, JsValue, _]]]
  ): WebSocket = {
    WebSocket.acceptOrResult[JsValue, JsValue] { requestHeader ⇒
      cookieService.getTokenFromRequest(requestHeader) match {
        case Some(token) ⇒
          jwtService.validateToken(token) match {
            case Success(userToken) ⇒
              block(userToken)
            case Failure(ex) ⇒
              Future.successful(
                Left(
                  Results
                    .Unauthorized(s"Invalid token: ${ex.getMessage}")
                    .withCookies(cookieService.createExpiredAuthCookie())
                )
              )
          }
        case None ⇒
          Future.successful(
            Left(Results.Unauthorized("No authentication token found"))
          )
      }
    }
  }
}
