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
//
//import java.time.LocalDateTime
//import scala.concurrent.{ExecutionContext, Future}
//
//class WorkspaceServiceSpec
//    extends AsyncWordSpec
//        with Matchers
//        with MockitoSugar {
//
//    // Mock repo
//    val mockRepo: WorkspaceRepository = mock[WorkspaceRepository]
//    val service = new WorkspaceService(mockRepo)(ExecutionContext.global)
//
//    "WorkspaceService.createWorkspace" should {
//        "create a workspace and return its ID" in {
//            val req =
//                CreateWorkspaceRequest(name = "Test WS", description = Some("desc"))
//
//            when(mockRepo.createWithOwner(any[Workspace], any[Int]))
//                .thenReturn(Future successful 123)
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
//                createdAt = Some(LocalDateTime.now()),
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
//            when(mockRepo.getWorkspaceById(999)) thenReturn (Future successful None)
//
//            recoverToExceptionIf[AppException] {
//                service.updateWorkspace(999, req, updatedBy = 1)
//            }.map { ex =>
//                ex.statusCode shouldBe Status.NOT_FOUND
//                ex.message shouldBe "Workspace not found"
//            }
//        }
//    }
//}
