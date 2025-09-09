package controllers

import dto.request.project.CreateProjectRequest
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

class ProjectControllerSpec
  extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with ScalaFutures
    with BeforeAndAfterAll {

  override implicit def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "config.resource" -> "application.test.conf",
        "slick.dbs.default.db.url" -> s"jdbc:h2:mem:projecttest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
      )
      .build()
  }

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
      projectService.createProject(CreateProjectRequest("Initial Project"), 1, 1)
    )
  }

  "ProjectController" should {

    "create project successfully" in {
      val body = Json.toJson(CreateProjectRequest("New Project"))
      val request = FakeRequest(POST, "/api/workspaces/1/projects")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String] must include("Project created successfully")
    }

    "fail when workspace does not exist" in {
      val nonExistentWorkspaceId = 9999
      val body = Json.toJson(CreateProjectRequest("Orphan Project"))

      val request = FakeRequest(POST, s"/api/workspaces/$nonExistentWorkspaceId/projects")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val ex = intercept[AppException] {
        await(route(app, request).get)
      }
      ex.statusCode mustBe NOT_FOUND
      ex.message must include("Workspace not found")
    }

    "retrieve all projects in workspace" in {
      val request = FakeRequest(GET, "/api/workspaces/1/projects")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Projects retrieved"
    }

    "complete project successfully" in {
      val projectId = 1
      val request = FakeRequest(PATCH, s"/api/projects/$projectId/complete")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Project completed successfully"
    }

    "fail to complete non-existent project" in {
      val nonExistentProjectId = 9999
      val request = FakeRequest(PATCH, s"/api/projects/$nonExistentProjectId/complete")
        .withCookies(Cookie(cookieName, fakeToken))

      val ex = intercept[AppException] {
        await(route(app, request).get)
      }
      ex.statusCode mustBe NOT_FOUND
      ex.message must include("Project not found or you are not the owner")
    }

//    "delete project successfully" in {
//      val projectId = 1
//      val completeRequest = FakeRequest(PATCH, s"/api/projects/$projectId/complete")
//        .withCookies(Cookie(cookieName, fakeToken))
//      status(route(app, completeRequest).get) mustBe OK
//
//      val request = FakeRequest(PATCH, s"/api/projects/$projectId/delete")
//        .withCookies(Cookie(cookieName, fakeToken))
//      val result = route(app, request).get
//
//      status(result) mustBe OK
//      (contentAsJson(result) \ "message").as[String] mustBe "Project deleted successfully"
//    }

    "reopen project successfully" in {
      val projectId = 1
      val request = FakeRequest(PATCH, s"/api/projects/$projectId/reopen")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Project reopened successfully"
    }

    "get completed projects by user" in {
      val request = FakeRequest(GET, "/api/projects/completed")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Completed projects retrieved"
    }

    "get all members in project successfully" in {
      val request = FakeRequest(GET, "/api/projects/1/members")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Project members retrieved"
    }

    "fail to get members for non-existent project" in {
      val nonExistentProjectId = 9999
      val request = FakeRequest(GET, s"/api/projects/$nonExistentProjectId/members")
        .withCookies(Cookie(cookieName, fakeToken))

      val ex = intercept[AppException] {
        await(route(app, request).get)
      }
      ex.statusCode mustBe NOT_FOUND
      ex.message must include("Project not found")
    }
  }
}
