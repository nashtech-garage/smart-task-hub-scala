package controllers

import dto.request.workspace.{CreateWorkspaceRequest, InviteUserIntoWorkspaceRequest, UpdateWorkspaceRequest}
import dto.response.ApiResponse
import exception.AppException
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{
  Action,
  AnyContent,
  MessagesAbstractController,
  MessagesControllerComponents
}
import services.WorkspaceService
import utils.WritesExtras.unitWrites
import validations.ValidationHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class WorkspaceController @Inject()(
  cc: MessagesControllerComponents,
  workspaceService: WorkspaceService,
  authenticatedActionWithUser: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ValidationHandler {

  /** POST /workspaces */
  def create(): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      // Extract messages from the request for validation
      // and error handling
      // This is necessary to ensure that the validation messages
      // are localized based on the user's language preference.
      // The `messages` object will be used in the validation methods
      // to provide localized error messages.
      implicit val messages: Messages = request.messages
      val createdBy = request.userToken.userId
      handleJsonValidation[CreateWorkspaceRequest](request.body) {
        createWorkspaceDto =>
          workspaceService
            .createWorkspace(createWorkspaceDto, createdBy)
            .map { _ =>
              Created(
                Json.toJson(ApiResponse[Unit]("Workspace created successfully"))
              )
            }
            .recover {
              case ex: AppException =>
                BadRequest(
                  Json.obj(
                    "message" -> "Duplicate workspace name",
                    "errors" -> Json
                      .arr(Json.obj("field" -> "name", "message" -> ex.message))
                  )
                )
            }
      }
    }

  /** GET /workspaces */
  def getAllWorkspaces: Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      workspaceService.getAllWorkspaces(userId).map { workspaces =>
        val apiResponse =
          ApiResponse.success("Workspaces retrieved", workspaces)
        Ok(Json.toJson(apiResponse))
      }
    }

  /** GET /workspaces/:id */
  def getWorkspaceById(id: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request ⇒
      val userId = request.userToken.userId
      workspaceService.getWorkspaceById(id, userId).map {
        case Some(workspace) =>
          val apiResponse =
            ApiResponse.success("Workspace retrieved ok", workspace)
          Ok(Json.toJson(apiResponse))
        case None => NotFound(Json.obj("error" -> s"Workspace $id not found"))
      }
    }

  /** DELETE /workspaces/:id */
  def deleteWorkspace(id: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request ⇒
      val userId = request.userToken.userId
      workspaceService.deleteWorkspace(id, userId).map {
        case true =>
          Ok(
            Json
              .toJson(
                ApiResponse.successNoData("Workspace deleted successfully")
              )
          )
        case false =>
          NotFound(
            Json.toJson(
              ApiResponse
                .errorNoData("Workspace not found or could not be deleted")
            )
          )
      }
    }

  /** PUT /workspaces/:id */
  def update(id: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val updatedBy = request.userToken.userId
      handleJsonValidation[UpdateWorkspaceRequest](request.body) {
        updateWorkspaceDto =>
          workspaceService
            .updateWorkspace(id, updateWorkspaceDto, updatedBy)
            .map { _ =>
              Ok(
                Json.toJson(ApiResponse[Unit]("Workspace updated successfully"))
              )
            }
      }
    }

  def getAllMembers(workspaceId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      workspaceService
          .getAllMembersInWorkspace(workspaceId, userId)
          .map { members =>
            val apiResponse = ApiResponse.success("Members retrieved successfully", members)
            Ok(Json.toJson(apiResponse))
          }
          .recover {
            case ex: AppException =>
              if (ex.statusCode == 403) {
                Forbidden(Json.toJson(ApiResponse[Unit](ex.message)))
              } else if (ex.statusCode == 404) {
                NotFound(Json.toJson(ApiResponse[Unit](ex.message)))
              } else {
                BadRequest(Json.toJson(ApiResponse[Unit](ex.message)))
              }
          }
    }

  def inviteUser(workspaceId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val invitorId = request.userToken.userId
      handleJsonValidation[InviteUserIntoWorkspaceRequest](request.body) {
        inviteUserIntoWorkspaceDto =>
          workspaceService
            .inviteUserToWorkspace(invitorId, workspaceId, inviteUserIntoWorkspaceDto)
            .map(
              _ =>
                Ok(
                  Json
                    .toJson(ApiResponse[Unit]("User invited successfully"))
              )
            )
      }

    }

  def removeMember(workspaceId: Int, memberId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val requesterId = request.userToken.userId
      workspaceService
        .removeMemberFromWorkspace(workspaceId, memberId, requesterId)
        .map { success =>
          if (success) {
            Ok(Json.toJson(ApiResponse[Unit]("Member removed successfully")))
          } else {
            NotFound(Json.obj("error" -> "Failed to remove member"))
          }
        }
        .recover {
          case ex: AppException =>
            if (ex.statusCode == 403) {
              Forbidden(Json.toJson(ApiResponse[Unit](ex.message)))
            } else if (ex.statusCode == 404) {
              NotFound(Json.toJson(ApiResponse[Unit](ex.message)))
            } else {
              BadRequest(Json.toJson(ApiResponse[Unit](ex.message)))
            }
        }
    }

  def leaveWorkspace(workspaceId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      workspaceService
        .leaveWorkspace(workspaceId, userId)
        .map { success =>
          if (success) {
            Ok(Json.toJson(ApiResponse[Unit]("Left workspace successfully")))
          } else {
            NotFound(Json.obj("error" -> "Failed to leave workspace"))
          }
        }
        .recover {
          case ex: AppException =>
            if (ex.statusCode == 404) {
              NotFound(Json.toJson(ApiResponse[Unit](ex.message)))
            } else {
              BadRequest(Json.toJson(ApiResponse[Unit](ex.message)))
            }
        }
    }
}
