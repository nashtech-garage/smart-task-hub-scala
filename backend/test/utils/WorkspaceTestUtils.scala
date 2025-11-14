package test.utils

import play.api.libs.json.Json
import services.UserToken
import exception.AppException

/**
  * Test utilities and helpers for workspace-related tests.
  * Provides common test data, builders, and utility methods.
  */
object WorkspaceTestUtils {

  /**
    * Test data builder for creating consistent test data across tests
    */
  object TestData {
    val testWorkspaceId = 100
    val testUserId = 1
    val testInviteeId = 2
    val testAdminId = 1
    val testMemberId = 3
    val testEmail = "testuser@example.com"
    val testWorkspaceName = "Test Workspace"
    val testWorkspaceDescription = "A test workspace for unit tests"
    val defaultAdminEmail = "admin@mail.com"
    val defaultAdminName = "Administrator"
    val cookieName = "auth_token"
  }

  /**
    * Create a valid user token for testing
    */
  def createUserToken(userId: Int, email: String = TestData.testEmail): UserToken = {
    UserToken(userId, "Test User", email)
  }

  /**
    * Create invite JSON payload
    */
  def createInviteJson(email: String): String = {
    Json.obj("email" -> email).toString()
  }

  /**
    * Create workspace create JSON payload
    */
  def createWorkspaceJson(name: String, description: Option[String] = None): String = {
    description match {
      case Some(desc) =>
        Json.obj(
          "name" -> name,
          "description" -> desc
        ).toString()
      case None =>
        Json.obj("name" -> name).toString()
    }
  }

  /**
    * Create workspace update JSON payload
    */
  def updateWorkspaceJson(name: String, description: Option[String] = None): String = {
    description match {
      case Some(desc) =>
        Json.obj(
          "name" -> name,
          "description" -> desc
        ).toString()
      case None =>
        Json.obj("name" -> name).toString()
    }
  }

  /**
    * Verify JSON response contains expected fields
    */
  def assertJsonHasFields(json: String, fields: String*): Boolean = {
    val parsedJson = Json.parse(json)
    fields.forall(field => (parsedJson \ field).toOption.isDefined || (parsedJson \ field).toOption.isDefined)
  }

  /**
    * Extract field value from JSON response
    */
  def getJsonFieldValue(json: String, fieldPath: String): Option[String] = {
    val parsedJson = Json.parse(json)
    (parsedJson \ fieldPath).toOption.flatMap(_.asOpt[String])
  }

  /**
    * Common test scenarios
    */
  object Scenarios {

    /**
      * Scenario: User invites another user to workspace
      * Expected: Success
      * Prerequisites:
      * - Inviter is a member of the workspace
      * - Target user exists
      * - Target user is not already a member
      */
    val inviteUserSuccess = "User invites another user to workspace successfully"

    /**
      * Scenario: User tries to remove a member
      * Expected: Success if user is admin
      * Prerequisites:
      * - Requester is admin of the workspace
      * - Target member exists in workspace
      */
    val removeMemberSuccess = "Admin removes member from workspace successfully"

    /**
      * Scenario: Member tries to remove someone
      * Expected: Failure with 403 Forbidden
      * Prerequisites:
      * - Requester is a regular member
      */
    val removeMemberAccessDenied = "Regular member cannot remove other members"

    /**
      * Scenario: User leaves a workspace
      * Expected: Success
      * Prerequisites:
      * - User is a member of the workspace
      * - User is not the only admin
      */
    val leaveWorkspaceSuccess = "User leaves workspace successfully"

    /**
      * Scenario: Only admin tries to leave
      * Expected: May fail if they are the only admin
      * Prerequisites:
      * - User is the only admin
      */
    val leaveWorkspaceAsOnlyAdmin = "Only admin tries to leave workspace"
  }

  /**
    * Error messages for assertions
    */
  object ErrorMessages {
    val userNotFound = "User not found"
    val workspaceNotFound = "Workspace not found"
    val noPermission = "You don't have permission"
    val alreadyMember = "User is already a member of the workspace"
    val notMember = "You are not a member of this workspace"
    val invalidEmail = "Invalid email format"
    val duplicateWorkspaceName = "Duplicate workspace name"
  }
}

/**
  * Test assertion helpers
  */
object TestAssertions {

  /**
    * Assert that an exception has the expected status code
    */
  def assertExceptionStatus(ex: Exception, expectedStatus: Int): Boolean = {
    ex match {
      case ae: AppException => ae.statusCode == expectedStatus
      case _ => false
    }
  }

  /**
    * Assert that an exception message contains expected text
    */
  def assertExceptionMessage(ex: Exception, expectedText: String): Boolean = {
    Option(ex.getMessage).exists(_.contains(expectedText))
  }

  /**
    * Assert HTTP response status is successful (2xx)
    */
  def assertSuccessStatus(status: Int): Boolean = {
    status >= 200 && status < 300
  }

  /**
    * Assert HTTP response status is client error (4xx)
    */
  def assertClientErrorStatus(status: Int): Boolean = {
    status >= 400 && status < 500
  }

  /**
    * Assert HTTP response status is server error (5xx)
    */
  def assertServerErrorStatus(status: Int): Boolean = {
    status >= 500 && status < 600
  }
}

/**
  * Mock data builders for testing
  */
object MockDataBuilders {

  /**
    * Create a mock Workspace entity
    */
  def createMockWorkspace(
    id: Int = 1,
    name: String = "Test Workspace",
    createdBy: Int = 1
  ): models.entities.Workspace = {
    models.entities.Workspace(
      id = Some(id),
      name = name,
      description = Some("Test description"),
      createdBy = Some(createdBy),
      createdAt = Some(java.time.Instant.now()),
      updatedBy = None,
      updatedAt = None
    )
  }

  /**
    * Create a mock User entity
    */
  def createMockUser(
    id: Int = 1,
    email: String = "test@example.com",
    name: String = "Test User",
    password: String = "password"
  ): models.entities.User = {
    models.entities.User(
      id = Some(id),
      name = name,
      email = email,
      password = password
    )
  }

  /**
    * Create a mock UserWorkspace entity
    */
  def createMockUserWorkspace(
    userId: Int = 1,
    workspaceId: Int = 1,
    role: models.Enums.UserWorkspaceRole.UserWorkspaceRole = models.Enums.UserWorkspaceRole.member
  ): models.entities.UserWorkspace = {
    models.entities.UserWorkspace(
      userId = userId,
      workspaceId = workspaceId,
      role = role
    )
  }
}
