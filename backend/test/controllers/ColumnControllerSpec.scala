package controllers

import dto.request.column.{
  CreateColumnRequest,
  UpdateColumnPositionRequest,
  UpdateColumnRequest
}
import exception.AppException
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration}
import services.{JwtService, ProjectService, UserToken, WorkspaceService}

class ColumnControllerSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with ScalaFutures
    with BeforeAndAfterAll {

  override implicit def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "config.resource" -> "application.test.conf",
        "slick.dbs.default.db.url" -> s"jdbc:h2:mem:columntest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
      )
      .build()
  }

  // config & token
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

  "ColumnController" should {

    "should find columns successfully" in {
      val request = FakeRequest(GET, "/api/projects/1/columns").withCookies(
        Cookie(cookieName, fakeToken)
      )
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Columns retrieved"
    }

    "should get columns fail when project not found or not active" in {
      val nonExistentProjectId = 9999

      val request =
        FakeRequest(GET, s"/api/projects/$nonExistentProjectId/columns")
          .withCookies(Cookie(cookieName, fakeToken))

      val resultFut = route(app, request).get

      val ex = intercept[AppException] {
        await(resultFut)
      }

      ex.statusCode mustBe NOT_FOUND
      ex.message must include(
        s"Project $nonExistentProjectId is not exists or not active"
      )
    }

    "should create column successfully" in {
      val anotherPosition = 100
      val body = Json.toJson(CreateColumnRequest("New Column", anotherPosition))
      val request = FakeRequest(POST, "/api/projects/1/columns")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String] must include(
        "Column created successfully"
      )
    }

    "should fail when column position already exists" in {
      val duplicatePosition = 1
      val body =
        Json.toJson(CreateColumnRequest("New Column", duplicatePosition))

      val request = FakeRequest(POST, "/api/projects/1/columns")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val resultFut = route(app, request).get

      val ex = intercept[AppException] {
        await(resultFut)
      }
      ex.statusCode mustBe CONFLICT
      ex.message must include(
        s"Column position $duplicatePosition already exists in project 1"
      )
    }

    "should column fail when project not found or not active" in {
      val nonExistentProjectId = 9999
      val body = Json.toJson(CreateColumnRequest("Orphan Column", 1))

      val request =
        FakeRequest(POST, s"/api/projects/$nonExistentProjectId/columns")
          .withCookies(Cookie(cookieName, fakeToken))
          .withBody(body)

      val resultFut = route(app, request).get

      val ex = intercept[AppException] {
        await(resultFut)
      }

      ex.statusCode mustBe NOT_FOUND
      ex.message must include(
        s"Project $nonExistentProjectId is not found or not active"
      )
    }

    "update column successfully" in {
      val body = Json.toJson(UpdateColumnRequest("Updated Column"))
      val request = FakeRequest(PATCH, "/api/projects/1/columns/1")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)
        .withHeaders(CONTENT_TYPE -> "application/json")

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Column updated successfully"
    }

    "should update column fail when project not found or not active" in {
      val nonExistentProjectId = 9999
      val nonExistentColumnId = 1234

      val body = Json.toJson(UpdateColumnRequest("Updated name"))

      val request = FakeRequest(
        PATCH,
        s"/api/projects/$nonExistentProjectId/columns/$nonExistentColumnId"
      ).withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val resultFut = route(app, request).get

      val ex = intercept[AppException] {
        await(resultFut)
      }

      ex.statusCode mustBe NOT_FOUND
      ex.message must include(
        s"Column $nonExistentColumnId is not found or not active"
      )
    }

    "archive column successfully" in {
      val request = FakeRequest(PATCH, "/api/columns/1/archive")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Column archived successfully"
    }

    "restore column successfully" in {
      val request = FakeRequest(PATCH, "/api/columns/1/restore")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Column restored successfully"
    }

    "delete column successfully" in {
      val archiveRequest = FakeRequest(PATCH, "/api/columns/3/archive")
        .withCookies(Cookie(cookieName, fakeToken))
      route(app, archiveRequest).get

      val request = FakeRequest(DELETE, "/api/columns/3")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Column deleted successfully"
    }
  }

  "update column position successfully" in {
    val columnPosition = 100

    val body = Json.toJson(UpdateColumnPositionRequest(columnPosition))
    val request = FakeRequest(PATCH, "/api/projects/1/columns/1/position")
      .withCookies(Cookie(cookieName, fakeToken))
      .withBody(body)
      .withHeaders(CONTENT_TYPE -> "application/json")

    val result = route(app, request).get

    status(result) mustBe OK
    (contentAsJson(result) \ "message")
      .as[String] mustBe "Column position updated successfully"
  }
}
