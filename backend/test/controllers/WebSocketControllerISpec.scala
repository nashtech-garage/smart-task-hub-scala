package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Configuration}
import services.{JwtService, ProjectService, UserToken, WorkspaceService}

import scala.concurrent.{ExecutionContext, Future}

class WebSocketControllerISpec
    extends PlaySpec
    with Injecting
    with GuiceOneAppPerSuite
    with ScalaFutures
    with BeforeAndAfterAll {

  override implicit def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "config.resource" -> "application.test.conf",
        "slick.dbs.default.db.url"
          -> s"jdbc:h2:mem:websocket;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
      )
      .build()
  }

  implicit lazy val system: ActorSystem = app.injector.instanceOf[ActorSystem]
  implicit lazy val mat: Materializer = app.injector.instanceOf[Materializer]
  implicit lazy val ec: ExecutionContext =
    app.injector.instanceOf[ExecutionContext]
  lazy val controller: WebSocketController =
    app.injector.instanceOf[WebSocketController]

  lazy val config: Configuration = app.configuration
  lazy val defaultAdminEmail: String =
    config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
  lazy val defaultAdminName: String =
    config.getOptional[String]("admin.name").getOrElse("Administrator")
  lazy val cookieName: String =
    config.getOptional[String]("cookie.name").getOrElse("auth_token")

  def fakeToken: String = {
    val jwtService = inject[JwtService]
    jwtService
      .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
      .getOrElse(throw new RuntimeException("JWT token not generated"))
  }

  override def beforeAll(): Unit = {
    val workspaceService = inject[WorkspaceService]
    val projectService = inject[ProjectService]

    await(
      workspaceService.createWorkspace(
        dto.request.workspace.CreateWorkspaceRequest("Workspace test"),
        1
      )
    )

    await(
      // Create a project with default columns
      projectService.createProject(
        dto.request.project.CreateProjectRequest("Project test"),
        1,
        1
      )
    )
  }

  "WebSocketController#joinProject" should {

    "reject if user is not in project" in {
      // fake user token → projectService trả về false

      val request = FakeRequest(GET, "/ws/projects/123")
      .withCookies(
        Cookie(cookieName, fakeToken)
      )
      val inExpectedProjectId = -1

      val wsFuture = controller.joinProject(inExpectedProjectId).apply(request)

      val either = await(wsFuture)

      either.isLeft mustBe true

      val result = either.left.get

      status(Future.successful(result)) mustBe FORBIDDEN
      contentAsString(Future.successful(result)) must include(
        "User is not a member of this project"
      )

    }

    "accept if user is in project" in {
      // giả lập user trong project → cần mock ProjectService
      // Ở đây mình dùng application.conf test để override

      val request = FakeRequest(GET, "/ws/projects/1")
        .withCookies(
          Cookie(cookieName, fakeToken)
        )
      val wsFuture = controller.joinProject(1).apply(request)

      val either = await(wsFuture)

      either.isRight mustBe true

      val flow = either.toOption.get

      flow must not be null
    }
  }
}
