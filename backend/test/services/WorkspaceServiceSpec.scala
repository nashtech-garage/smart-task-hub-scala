//package services
//
//import dto.request.workspace.{CreateWorkspaceRequest, UpdateWorkspaceRequest}
//import exception.AppException
//import models.entities.Workspace
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Mockito._
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AsyncWordSpec
//import org.scalatestplus.mockito.MockitoSugar
//import play.api.http.Status
//import repositories.WorkspaceRepository
//import test.utils.MockDataBuilders
//
//import java.time.Instant
//import scala.concurrent.{ExecutionContext, Future}
//
//class WorkspaceServiceSpec
//    extends AsyncWordSpec
//        with Matchers
//        with MockitoSugar {
//
//    // Mock dependencies
//    val mockUserRepo = mock[repositories.UserRepository]
//    val mockEmailService = mock[services.EmailService]
//    val mockDbConfigProvider = mock[play.api.db.slick.DatabaseConfigProvider]
//    val mockRepo: WorkspaceRepository = mock[WorkspaceRepository]
//
//    val service = new WorkspaceService(
//        mockRepo,
//        mockUserRepo,
//        mockEmailService,
//        mockDbConfigProvider
//    )(ExecutionContext.global)
//
//    "WorkspaceService.createWorkspace" should {
//        "create a workspace and return its ID" in {
//            val req =
//                CreateWorkspaceRequest(name = "Test WS", description = Some("desc"))
//
//            when(mockRepo.createWithOwner(any[Workspace], any[Int]))
//                .thenReturn(Future.successful(123))
//
//            service.createWorkspace(req, createdBy = 1).map { id =>
//                id shouldBe 123
//            }
//        }
//    }
//
//    "WorkspaceService.updateWorkspace" should {
//        "update an existing workspace" in {
//            val existingWs = Workspace(
//                id = Some(1),
//                name = "Old Name",
//                description = Some("Old"),
//                createdBy = Some(1),
//                createdAt = Some(Instant.now()),
//                updatedBy = None,
//                updatedAt = None
//            )
//
//            val req =
//                UpdateWorkspaceRequest(name = "New Name", description = Some("Updated"))
//
//            when(mockRepo.getWorkspaceById(1))
//                .thenReturn(Future.successful(Some(existingWs)))
//
//            when(mockRepo.update(any[Workspace]))
//                .thenReturn(Future.successful(1))
//
//            service.updateWorkspace(1, req, updatedBy = 2).map { rows =>
//                rows shouldBe 1
//            }
//        }
//
//        "fail when workspace not found" in {
//            val req =
//                UpdateWorkspaceRequest(name = "Does not matter", description = None)
//
//            when(mockRepo.getWorkspaceById(999))
//                .thenReturn(Future.successful(None))
//
//            recoverToExceptionIf[AppException] {
//                service.updateWorkspace(999, req, updatedBy = 1)
//            }.map { ex =>
//                ex.statusCode shouldBe Status.NOT_FOUND
//                ex.message shouldBe "Workspace not found"
//            }
//        }
//    }
//
//    "WorkspaceService.removeMemberFromWorkspace" should {
//
//        "remove a member successfully when requester is admin" in {
//            when(mockRepo.getUserWorkspaceRole(1, 10))
//                .thenReturn(Future.successful(Some("admin")))
//
//            when(mockRepo.isUserInActiveWorkspace(1, 20))
//                .thenReturn(Future.successful(true))
//
//            when(mockRepo.removeUserFromWorkspace(1, 20))
//                .thenReturn(Future.successful(1))
//
//            service.removeMemberFromWorkspace(1, 20, 10).map { result =>
//                result shouldBe true
//            }
//        }
//
//        "fail when requester is not an admin" in {
//            when(mockRepo.getUserWorkspaceRole(1, 10))
//                .thenReturn(Future.successful(Some("member")))
//
//            recoverToExceptionIf[AppException] {
//                service.removeMemberFromWorkspace(1, 20, 10)
//            }.map { ex =>
//                ex.statusCode shouldBe Status.FORBIDDEN
//                ex.message should include("don't have permission")
//            }
//        }
//
//        "fail when member is not in workspace" in {
//            when(mockRepo.getUserWorkspaceRole(1, 10))
//                .thenReturn(Future.successful(Some("admin")))
//
//            when(mockRepo.isUserInActiveWorkspace(1, 20))
//                .thenReturn(Future.successful(false))
//
//            recoverToExceptionIf[AppException] {
//                service.removeMemberFromWorkspace(1, 20, 10)
//            }.map { ex =>
//                ex.statusCode shouldBe Status.NOT_FOUND
//                ex.message should include("not a member")
//            }
//        }
//    }
//
//    "WorkspaceService.leaveWorkspace" should {
//
//        "allow a normal member to leave successfully" in {
//            when(mockRepo.isUserInActiveWorkspace(1, 30))
//                .thenReturn(Future.successful(true))
//
//            when(mockRepo.getUserWorkspaceRole(1, 30))
//                .thenReturn(Future.successful(Some("member")))
//
//            when(mockRepo.removeUserFromWorkspace(1, 30))
//                .thenReturn(Future.successful(1))
//
//            service.leaveWorkspace(1, 30).map { result =>
//                result shouldBe true
//            }
//        }
//
//        "fail when user is not in workspace" in {
//            when(mockRepo.isUserInActiveWorkspace(1, 40))
//                .thenReturn(Future.successful(false))
//
//            recoverToExceptionIf[AppException] {
//                service.leaveWorkspace(1, 40)
//            }.map { ex =>
//                ex.statusCode shouldBe Status.NOT_FOUND
//                ex.message should include("not a member")
//            }
//        }
//
//        "allow an admin to leave if not restricted (simplified case)" in {
//            when(mockRepo.isUserInActiveWorkspace(1, 50))
//                .thenReturn(Future.successful(true))
//
//            when(mockRepo.getUserWorkspaceRole(1, 50))
//                .thenReturn(Future.successful(Some("admin")))
//
//            when(mockRepo.removeUserFromWorkspace(1, 50))
//                .thenReturn(Future.successful(1))
//
//            service.leaveWorkspace(1, 50).map { result =>
//                result shouldBe true
//            }
//        }
//    }
//}
