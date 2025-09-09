package controllers

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration}
import services.{JwtService, UserToken}

class WorkspaceControllerSpec
    extends PlaySpec
        with GuiceOneAppPerTest
        with Injecting
        with ScalaFutures
        with BeforeAndAfterAll {

    // Load test configuration from application.test.conf
    override implicit def fakeApplication(): Application = {
        new GuiceApplicationBuilder()
            .configure("config.resource" -> "application.test.conf")
            .build()
    }

    // Get config from app after app init
    lazy val config: Configuration = app.configuration
    lazy val defaultAdminEmail: String =
        config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
    lazy val defaultAdminName: String =
        config.getOptional[String]("admin.name").getOrElse("Administrator")
    lazy val cookieName: String =
        config.getOptional[String]("cookie.name").getOrElse("auth_token")

    "WorkspaceController" should {

        "create workspace successfully" in {
            val createJson = Json.obj(
                "name" -> "Test Workspace",
                "description" -> "Integration test"
            )

            // Fake token (create valid token)
            val jwtService = inject[JwtService]
            val token = jwtService
                .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
                .getOrElse(throw new RuntimeException("JWT token not generated"))

            // Create request with JSON body and cookie
            val request = FakeRequest(POST, "/api/workspaces")
                .withJsonBody(createJson)
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe CREATED
            (contentAsJson(result) \ "message")
                .as[String] mustBe "Workspace created successfully"
        }

        "update workspace successfully" in {
            val updateJson = Json.obj(
                "name" -> "Updated Workspace",
                "description" -> "Updated description"
            )

            // Insert workspace test before update
            val wsService = inject[services.WorkspaceService]
            val workspaceId = await(
                wsService.createWorkspace(
                    dto.request.workspace
                        .CreateWorkspaceRequest("Init Workspace", Some("desc")),
                    1
                )
            )
            // Create valid token
            val jwtService = inject[JwtService]
            val token = jwtService
                .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
                .getOrElse(throw new RuntimeException("JWT token not generated"))

            // Create request with JSON body and cookie
            // Use the workspaceId from the created workspace
            val request = FakeRequest(PUT, s"/api/workspaces/$workspaceId")
                .withJsonBody(updateJson)
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe OK
            (contentAsJson(result) \ "message")
                .as[String] mustBe "Workspace updated successfully"
        }

        "get all workspaces successfully" in {
            // First create some test workspaces
            val wsService = inject[services.WorkspaceService]
            await(
                wsService.createWorkspace(
                    dto.request.workspace
                        .CreateWorkspaceRequest("Test Workspace 1", Some("desc1")),
                    1
                )
            )
            await(
                wsService.createWorkspace(
                    dto.request.workspace
                        .CreateWorkspaceRequest("Test Workspace 2", Some("desc2")),
                    1
                )
            )

            // Create valid token
            val jwtService = inject[JwtService]
            val token = jwtService
                .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
                .getOrElse(throw new RuntimeException("JWT token not generated"))

            // Create request to get all workspaces with authentication
            val request = FakeRequest(GET, "/api/workspaces")
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe OK
            (contentAsJson(result) \ "message")
                .as[String] mustBe "Workspaces retrieved"

            // Verify that the response contains workspaces data
            val dataNode = (contentAsJson(result) \ "data")
            dataNode.isDefined mustBe true
        }

        "get workspace by id successfully" in {
            // Create a test workspace first
            val wsService = inject[services.WorkspaceService]
            val workspaceId = await(
                wsService.createWorkspace(
                    dto.request.workspace
                        .CreateWorkspaceRequest("New Workspace", Some("test desc")),
                    1
                )
            )

            // Create valid token
            val jwtService = inject[JwtService]
            val token = jwtService
                .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
                .getOrElse(throw new RuntimeException("JWT token not generated"))

            // Create request to get workspace by id with authentication
            val request = FakeRequest(GET, s"/api/workspaces/$workspaceId")
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe OK
            (contentAsJson(result) \ "message")
                .as[String] mustBe "Workspace retrieved ok"

            // Verify that the response contains the workspace data
            val workspaceData = (contentAsJson(result) \ "data")
            workspaceData.isDefined mustBe true
        }

        "return 404 when getting workspace by non-existent id" in {
            val nonExistentId = 99999

            // Create valid token
            val jwtService = inject[JwtService]
            val token = jwtService
                .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
                .getOrElse(throw new RuntimeException("JWT token not generated"))

            // Create request to get workspace by non-existent id with authentication
            val request = FakeRequest(GET, s"/api/workspaces/$nonExistentId")
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe NOT_FOUND
            (contentAsJson(result) \ "error")
                .as[String] mustBe s"Workspace $nonExistentId not found"
        }

        "delete workspace successfully" in {
            // Create a test workspace first
            val wsService = inject[services.WorkspaceService]
            val workspaceId = await(
                wsService.createWorkspace(
                    dto.request.workspace
                        .CreateWorkspaceRequest("Workspace to Delete", Some("will be deleted")),
                    1
                )
            )

            // Create valid token
            val jwtService = inject[JwtService]
            val token = jwtService
                .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
                .getOrElse(throw new RuntimeException("JWT token not generated"))

            // Create request to delete workspace with authentication
            val request = FakeRequest(DELETE, s"/api/workspaces/$workspaceId")
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe OK
            (contentAsJson(result) \ "message")
                .as[String] mustBe "Workspace deleted successfully"
        }

        "return 404 when deleting non-existent workspace" in {
            val nonExistentId = 99999

            // Create valid token
            val jwtService = inject[JwtService]
            val token = jwtService
                .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
                .getOrElse(throw new RuntimeException("JWT token not generated"))

            // Create request to delete non-existent workspace with authentication
            val request = FakeRequest(DELETE, s"/api/workspaces/$nonExistentId")
                .withCookies(Cookie(cookieName, token))

            // Route the request to the controller
            val result = route(app, request).get

            // Check the response status and content
            status(result) mustBe NOT_FOUND
            (contentAsJson(result) \ "message")
                .as[String] mustBe "Workspace not found or could not be deleted"
        }
    }
}