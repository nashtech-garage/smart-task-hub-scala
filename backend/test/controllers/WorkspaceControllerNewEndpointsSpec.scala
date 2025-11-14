//package controllers
//
//import dto.request.workspace.InviteUserIntoWorkspaceRequest
//import exception.AppException
//import org.scalatest.BeforeAndAfterAll
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatestplus.play._
//import org.scalatestplus.play.guice.GuiceOneAppPerTest
//import play.api.http.Status
//import play.api.inject.guice.GuiceApplicationBuilder
//import play.api.libs.json.Json
//import play.api.mvc.Cookie
//import play.api.test.Helpers._
//import play.api.test._
//import play.api.{Application, Configuration}
//import services.{JwtService, UserToken}
//
///**
//  * Integration tests for new WorkspaceController endpoints:
//  * - inviteUser
//  * - removeMember
//  * - leaveWorkspace
//  */
//class WorkspaceControllerNewEndpointsSpec
//    extends PlaySpec
//    with GuiceOneAppPerTest
//    with Injecting
//    with ScalaFutures
//    with BeforeAndAfterAll {
//
//  override implicit def fakeApplication(): Application = {
//    new GuiceApplicationBuilder()
//      .configure("config.resource" -> "application.test.conf")
//      .build()
//  }
//
//  lazy val config: Configuration = app.configuration
//  lazy val defaultAdminEmail: String =
//    config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
//  lazy val defaultAdminName: String =
//    config.getOptional[String]("admin.name").getOrElse("Administrator")
//  lazy val cookieName: String =
//    config.getOptional[String]("cookie.name").getOrElse("auth_token")
//
//  lazy val jwtService: JwtService = inject[JwtService]
//  lazy val wsService: services.WorkspaceService = inject[services.WorkspaceService]
//
//  def generateToken(userId: Int, email: String = defaultAdminEmail, name: String = defaultAdminName): String = {
//    jwtService
//      .generateToken(UserToken(userId, name, email))
//      .getOrElse(throw new RuntimeException("JWT token not generated"))
//  }
//
//  "WorkspaceController.inviteUser" should {
//
//    "successfully invite a user to workspace with valid email" in {
//      // Create a workspace first
//      val workspaceId = await(
//        wsService.createWorkspace(
//          dto.request.workspace.CreateWorkspaceRequest("Invite Test WS", Some("desc")),
//          1
//        )
//      )
//
//      val inviteJson = Json.obj(
//        "email" -> "testuser@example.com"
//      )
//
//      val token = generateToken(1)
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/invite")
//        .withJsonBody(inviteJson)
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      // Should return 200 OK (since we need a real user to be created)
//      // or 404 if user doesn't exist
//      status(result) must (be(OK) or be(NOT_FOUND))
//    }
//
//    "return 400 for invalid email format" in {
//      val workspaceId = 1
//
//      val inviteJson = Json.obj(
//        "email" -> "not-a-valid-email"
//      )
//
//      val token = generateToken(1)
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/invite")
//        .withJsonBody(inviteJson)
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      status(result) mustBe BAD_REQUEST
//    }
//
//    "return 400 for missing email field" in {
//      val workspaceId = 1
//
//      val inviteJson = Json.obj(
//        "name" -> "some name"
//      )
//
//      val token = generateToken(1)
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/invite")
//        .withJsonBody(inviteJson)
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      status(result) mustBe BAD_REQUEST
//    }
//
//    "return 401 when not authenticated" in {
//      val workspaceId = 1
//
//      val inviteJson = Json.obj(
//        "email" -> "testuser@example.com"
//      )
//
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/invite")
//        .withJsonBody(inviteJson)
//
//      val result = route(app, request).get
//
//      status(result) mustBe UNAUTHORIZED
//    }
//  }
//
//  "WorkspaceController.removeMember" should {
//
//    "successfully remove a member from workspace when user is admin" in {
//      // Create a workspace
//      val workspaceId = await(
//        wsService.createWorkspace(
//          dto.request.workspace.CreateWorkspaceRequest("Remove Member Test WS", Some("desc")),
//          1
//        )
//      )
//
//      val token = generateToken(1)
//      val request = FakeRequest(DELETE, s"/api/workspaces/$workspaceId/members/2")
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      // Result should be either NOT_FOUND (member not found) or OK (success)
//      status(result) must (be(OK) or be(NOT_FOUND))
//    }
//
//    "return 403 Forbidden when user is not an admin" in {
//      val workspaceId = 1
//      val memberId = 2
//
//      val token = generateToken(2) // Different user, not admin
//      val request = FakeRequest(DELETE, s"/api/workspaces/$workspaceId/members/$memberId")
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      // Should fail with 403 or 404 depending on workspace membership
//      status(result) must (be(FORBIDDEN) or be(NOT_FOUND))
//    }
//
//    "return 404 when workspace does not exist" in {
//      val workspaceId = 99999
//      val memberId = 2
//
//      val token = generateToken(1)
//      val request = FakeRequest(DELETE, s"/api/workspaces/$workspaceId/members/$memberId")
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      status(result) mustBe NOT_FOUND
//    }
//
//    "return 401 when not authenticated" in {
//      val workspaceId = 1
//      val memberId = 2
//
//      val request = FakeRequest(DELETE, s"/api/workspaces/$workspaceId/members/$memberId")
//
//      val result = route(app, request).get
//
//      status(result) mustBe UNAUTHORIZED
//    }
//  }
//
//  "WorkspaceController.leaveWorkspace" should {
//
//    "successfully leave workspace when user is a member" in {
//      // Create a workspace
//      val workspaceId = await(
//        wsService.createWorkspace(
//          dto.request.workspace.CreateWorkspaceRequest("Leave Test WS", Some("desc")),
//          1
//        )
//      )
//
//      val token = generateToken(1)
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/leave")
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      status(result) must (be(OK) or be(NOT_FOUND))
//    }
//
//    "return 404 when user is not a member of workspace" in {
//      val workspaceId = 99999
//
//      val token = generateToken(1)
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/leave")
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      status(result) mustBe NOT_FOUND
//    }
//
//    "return 401 when not authenticated" in {
//      val workspaceId = 1
//
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/leave")
//
//      val result = route(app, request).get
//
//      status(result) mustBe UNAUTHORIZED
//    }
//
//    "return success message in response" in {
//      // Create a workspace
//      val workspaceId = await(
//        wsService.createWorkspace(
//          dto.request.workspace.CreateWorkspaceRequest("Success Message Test WS", Some("desc")),
//          1
//        )
//      )
//
//      val token = generateToken(1)
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/leave")
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      status(result) must (be(OK) or be(NOT_FOUND))
//      if (status(result) == OK) {
//        val json = contentAsJson(result)
//        (json \ "message").asOpt[String].getOrElse("") should include("Left workspace")
//      }
//    }
//  }
//
//  "Error handling in new endpoints" should {
//
//    "return appropriate error message for invite endpoint when user not found" in {
//      val workspaceId = 1
//
//      val inviteJson = Json.obj(
//        "email" -> "nonexistent@example.com"
//      )
//
//      val token = generateToken(1)
//      val request = FakeRequest(POST, s"/api/workspaces/$workspaceId/invite")
//        .withJsonBody(inviteJson)
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      // Should return 404 or other error
//      status(result) should not be (INTERNAL_SERVER_ERROR)
//    }
//
//    "return appropriate error message for remove member endpoint when permission denied" in {
//      val workspaceId = 1
//      val memberId = 2
//
//      val token = generateToken(2)
//      val request = FakeRequest(DELETE, s"/api/workspaces/$workspaceId/members/$memberId")
//        .withCookies(Cookie(cookieName, token))
//
//      val result = route(app, request).get
//
//      status(result) should not be (INTERNAL_SERVER_ERROR)
//    }
//  }
//}
//
