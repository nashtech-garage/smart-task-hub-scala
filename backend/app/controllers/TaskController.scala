package controllers

import dto.request.task.{AssignMemberRequest, CreateTaskRequest, UpdateTaskPositionRequest, UpdateTaskRequest}
import dto.response.ApiResponse
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import services.TaskService
import utils.WritesExtras.unitWrites
import validations.ValidationHandler

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TaskController @Inject()(
  cc: MessagesControllerComponents,
  taskService: TaskService,
  authenticatedActionWithUser: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext)
    extends MessagesAbstractController(cc)
    with ValidationHandler {

  def create(columnId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val createdBy = request.userToken.userId
      handleJsonValidation[CreateTaskRequest](request.body) { createColumnDto =>
        taskService
          .createNewTask(createColumnDto, columnId, createdBy)
          .map { taskId =>
            Created(
              Json.toJson(
                ApiResponse[Unit](s"Task created successfully with ID: $taskId")
              )
            )
          }
      }
    }

  def update(taskId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val updatedBy = request.userToken.userId
      handleJsonValidation[UpdateTaskRequest](request.body) { updateTaskDto =>
        taskService
          .updateTask(taskId, updateTaskDto, updatedBy)
          .map { _ =>
            Ok(Json.toJson(ApiResponse[Unit](s"Task updated successfully")))
          }
      }
    }

  def getById(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      taskService
        .getTaskDetailById(taskId, userId)
        .map { taskDetail =>
          Ok(
            Json.toJson(
              ApiResponse.success("Task retrieved successfully", taskDetail)
            )
          )
        }
    }

  def archive(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val archivedBy = request.userToken.userId
      taskService
        .archiveTask(taskId, archivedBy)
        .map { _ =>
          Ok(Json.toJson(ApiResponse[Unit](s"Task archived successfully")))
        }
    }

  def restore(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val restoredBy = request.userToken.userId
      taskService
        .restoreTask(taskId, restoredBy)
        .map { _ =>
          Ok(Json.toJson(ApiResponse[Unit](s"Task restored successfully")))
        }
    }

  def delete(taskId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val deletedBy = request.userToken.userId
      taskService
        .deleteTask(taskId, deletedBy)
        .map { _ =>
          Ok(Json.toJson(ApiResponse[Unit](s"Task deleted successfully")))
        }
    }

  def assignMember(projectId: Int, taskId: Int): Action[JsValue] = {
    authenticatedActionWithUser.async(parse.json) { request =>
      val assignedBy = request.userToken.userId
      handleJsonValidation[AssignMemberRequest](request.body) {
        assignMemberRequest =>
          val userId = assignMemberRequest.userId
          taskService
            .assignMemberToTask(projectId, taskId, userId, assignedBy)
            .map { _ =>
              Ok(
                Json.toJson(
                  ApiResponse[Unit](s"Member assigned to task successfully")
                )
              )
            }
      }
    }
  }

  def unassignMember(projectId: Int,
                     taskId: Int,
                     userId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val unassignedBy = request.userToken.userId
      taskService
        .unassignMemberFromTask(projectId, taskId, userId, unassignedBy)
        .map { _ =>
          Ok(
            Json.toJson(
              ApiResponse[Unit](s"Member unassigned from task successfully")
            )
          )
        }
    }

  def getArchivedTasks(projectId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      implicit val messages: Messages = request.messages
      val userId = request.userToken.userId
      taskService
        .getArchivedTask(projectId, userId)
        .map { tasks =>
          Ok(
            Json.toJson(
              ApiResponse
                .success("Archived tasks retrieved successfully", tasks)
            )
          )
        }
    }

  def getActiveTasksInProject(projectId: Int): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      implicit val messages: Messages = request.messages
      val userId = request.userToken.userId
      taskService
        .getActiveTasksInProject(projectId, userId)
        .map { tasks =>
          Ok(
            Json.toJson(
              ApiResponse.success("Active tasks retrieved successfully", tasks)
            )
          )
        }
    }

  def search(page: Int,
             size: Int,
             keyword: String,
             projectIds: Option[List[Int]]): Action[AnyContent] =
    authenticatedActionWithUser.async { request =>
      val userId = request.userToken.userId
      taskService
        .searchTasks(
          projectIds,
          Option(keyword).filter(_.nonEmpty),
          page,
          size,
          userId
        )
        .map { tasks =>
          Ok(
            Json.toJson(
              ApiResponse.success("Tasks retrieved successfully", tasks)
            )
          )
        }
    }

  def updatePosition(taskId: Int): Action[JsValue] =
    authenticatedActionWithUser.async(parse.json) { request =>
      implicit val messages: Messages = request.messages
      val updatedBy = request.userToken.userId
      handleJsonValidation[UpdateTaskPositionRequest](request.body) { updateTaskPositionDto =>
        taskService
          .updatePosition(taskId, updateTaskPositionDto, updatedBy)
          .map { _ =>
            Ok(Json.toJson(ApiResponse[Unit](s"Task position updated successfully")))
          }
      }
    }

}
