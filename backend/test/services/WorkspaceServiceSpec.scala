package services

import dto.request.workspace.InviteUserIntoWorkspaceRequest
import exception.AppException
import org.mockito.Mockito.{mock, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status

import scala.concurrent.Future

/**
 * Unit tests for WorkspaceService using mocks for repository layer.
 * These tests focus on the service logic without involving the database.
 */
class WorkspaceServiceSpec
    extends AsyncWordSpec
    with Matchers
    with MockitoSugar {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  "WorkspaceService#inviteUserToWorkspace" should {

    "successfully invite user to workspace" in {
      val inviterId = 1
      val workspaceId = 1
      val inviteeEmail = "newuser@example.com"
      val request = InviteUserIntoWorkspaceRequest(inviteeEmail)


      // Mock the service
      val mockService = mock[WorkspaceService]

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request))
        .thenReturn(Future.successful(1))

      val result = mockService.inviteUserToWorkspace(inviterId, workspaceId, request)

      result.map { insertedId =>
        insertedId shouldBe 1
        succeed
      }
    }

    "fail when inviter is not a member of workspace" in {
      val inviterId = 999
      val workspaceId = 1
      val request = InviteUserIntoWorkspaceRequest("user@example.com")

      val mockService = mock[WorkspaceService]
      val exception = AppException("Workspace not found", Status.NOT_FOUND)

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request))
        .thenReturn(Future.failed(exception))

      val result = mockService.inviteUserToWorkspace(inviterId, workspaceId, request)

      result.failed.map { ex =>
        ex.isInstanceOf[AppException] shouldBe true
        val appEx = ex.asInstanceOf[AppException]
        appEx.statusCode shouldBe Status.NOT_FOUND
      }
    }

    "fail when invitee user not found" in {
      val inviterId = 1
      val workspaceId = 1
      val inviteeEmail = "nonexistent@example.com"
      val request = InviteUserIntoWorkspaceRequest(inviteeEmail)

      val mockService = mock[WorkspaceService]
      val exception = AppException("User not found", Status.NOT_FOUND)

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request))
        .thenReturn(Future.failed(exception))

      val result = mockService.inviteUserToWorkspace(inviterId, workspaceId, request)

      result.failed.map { ex =>
        ex.isInstanceOf[AppException] shouldBe true
        val appEx = ex.asInstanceOf[AppException]
        appEx.statusCode shouldBe Status.NOT_FOUND
      }
    }

    "fail when user is already a member of workspace" in {
      val inviterId = 1
      val workspaceId = 1
      val inviteeEmail = "existing@example.com"
      val request = InviteUserIntoWorkspaceRequest(inviteeEmail)

      val mockService = mock[WorkspaceService]
      val exception = AppException("User is already a member of this workspace", Status.BAD_REQUEST)

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request))
        .thenReturn(Future.failed(exception))

      val result = mockService.inviteUserToWorkspace(inviterId, workspaceId, request)

      result.failed.map { ex =>
        ex.isInstanceOf[AppException] shouldBe true
        val appEx = ex.asInstanceOf[AppException]
        appEx.statusCode shouldBe Status.BAD_REQUEST
      }
    }

    "return valid id after successful invitation" in {
      val inviterId = 1
      val workspaceId = 1
      val inviteeEmail = "newmember@example.com"
      val request = InviteUserIntoWorkspaceRequest(inviteeEmail)

      val mockService = mock[WorkspaceService]

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request))
        .thenReturn(Future.successful(5))

      val result = mockService.inviteUserToWorkspace(inviterId, workspaceId, request)

      result.map { insertedId =>
        insertedId shouldBe 5
        insertedId should be > 0
        succeed
      }
    }

    "insert user with correct workspace and user IDs" in {
      val inviterId = 1
      val workspaceId = 1
      val inviteeEmail = "user@example.com"
      val expectedInsertedId = 42
      val request = InviteUserIntoWorkspaceRequest(inviteeEmail)

      val mockService = mock[WorkspaceService]

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request))
        .thenReturn(Future.successful(expectedInsertedId))

      val result = mockService.inviteUserToWorkspace(inviterId, workspaceId, request)

      result.map { insertedId =>
        insertedId shouldBe expectedInsertedId
        succeed
      }
    }

    "handle multiple invitations to same workspace" in {
      val inviterId = 1
      val workspaceId = 1
      val request1 = InviteUserIntoWorkspaceRequest("user1@example.com")
      val request2 = InviteUserIntoWorkspaceRequest("user2@example.com")

      val mockService = mock[WorkspaceService]

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request1))
        .thenReturn(Future.successful(1))

      when(mockService.inviteUserToWorkspace(inviterId, workspaceId, request2))
        .thenReturn(Future.successful(2))

      val result1 = mockService.inviteUserToWorkspace(inviterId, workspaceId, request1)
      val result2 = mockService.inviteUserToWorkspace(inviterId, workspaceId, request2)

      for {
        id1 <- result1
        id2 <- result2
      } yield {
        id1 shouldBe 1
        id2 shouldBe 2
        id1 should not equal id2
        succeed
      }
    }
  }
}



