package controllers

import dto.request.project.CreateProjectRequest
import dto.response.ApiResponse
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{
  Action,
  AnyContent,
  MessagesAbstractController,
  MessagesControllerComponents
}
import services.ProjectService
import utils.WritesExtras.unitWrites
import validations.ValidationHandler

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ProjectController @Inject()(
  cc: MessagesControllerComponents,
  projectService: ProjectService,
  authenticatedActionWithUser: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ValidationHandler {

  /** POST /workspaces/:workspaceId/projects */
  def create(workspaceId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val createdBy = request.userToken.userId
      handleJsonValidation[CreateProjectRequest](request.body) {
        createProjectDto =>
          projectService
            .createProject(createProjectDto, workspaceId, createdBy)
            .map { projectId =>
              Created(
                Json.toJson(
                  ApiResponse[Unit](
                    s"Project created successfully with ID: $projectId"
                  )
                )
              )
            }
      }
    }

  /** GET /workspaces/:workspaceId/projects */
  def getAll(workspaceId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      projectService.getProjectsByWorkspaceAndUser(workspaceId, userId).map {
        projects =>
          val apiResponse =
            ApiResponse(
              message = "Projects retrieved",
              data = Some(Json.toJson(projects))
            )
          Ok(Json.toJson(apiResponse))
      }
    }

  /** PATCH /projects/:projectId/complete */
  def completeProject(projectId: Int): Action[AnyContent] = {
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      projectService.completeProject(projectId, userId).map { _ =>
        Ok(Json.toJson(ApiResponse[Unit]("Project completed successfully")))
      }
    }
  }

  /** PATCH /projects/:projectId/delete */
  def deleteProject(projectId: Int): Action[AnyContent] = {
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      projectService.deleteProject(projectId, userId).map { _ =>
        Ok(Json.toJson(ApiResponse[Unit]("Project deleted successfully")))
      }
    }
  }

  /** PATCH /projects/:projectId/reopen */
  def reopenProject(projectId: Int): Action[AnyContent] = {
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      projectService.reopenProject(projectId, userId).map { _ =>
        Ok(Json.toJson(ApiResponse[Unit]("Project reopened successfully")))
      }
    }
  }

  /** GET /projects/completed */
  def getCompletedProjectsByUser: Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      projectService.getCompletedProjectsByUserId(userId).map { projects =>
        val apiResponse =
          ApiResponse.success("Completed projects retrieved", projects)
        Ok(Json.toJson(apiResponse))
      }
    }

  /** GET /projects/:projectId/members */
  def getAllMembersInProject(projectId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      projectService.getAllMembersInProject(projectId, userId).map { members =>
        val apiResponse =
          ApiResponse.success("Project members retrieved", members)
        Ok(Json.toJson(apiResponse))
      }
    }

    /** GET /projects/:projectId */
  def getProjectById(projectId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      projectService.getProjectById(projectId, userId).map { project =>
        val apiResponse =
          ApiResponse.success("Project retrieved", project)
        Ok(Json.toJson(apiResponse))
      }
    }
}
