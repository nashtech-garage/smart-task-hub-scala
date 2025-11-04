package services

import dto.request.project.{CreateProjectRequest, ImportProjectData}
import dto.response.project.{
  ProjectDetailResponse,
  ProjectResponse,
  ProjectSummariesResponse
}
import dto.response.user.UserInProjectResponse
import exception.AppException
import mappers.ProjectMapper
import models.Enums.ProjectStatus.ProjectStatus
import models.Enums.{ProjectStatus, ProjectVisibility}
import models.entities.Project
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import repositories.{
  ColumnRepository,
  ProjectRepository,
  TaskRepository,
  UserRepository,
  WorkspaceRepository
}
import slick.jdbc.JdbcProfile
import utils.JsonFormats._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProjectService @Inject()(
  projectRepository: ProjectRepository,
  workspaceRepository: WorkspaceRepository,
  columnRepository: ColumnRepository,
  taskRepository: TaskRepository,
  userRepository: UserRepository,
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def createProject(req: CreateProjectRequest,
                    workspaceId: Int,
                    createdBy: Int): scala.concurrent.Future[Int] = {

    val action = for {
      // check user is part of the active workspace
      existsUserInActiveWorkspace <- workspaceRepository
        .isUserInActiveWorkspace(workspaceId, createdBy)

      _ <- if (existsUserInActiveWorkspace) {
        DBIO.successful(())
      } else {
        DBIO.failed(
          AppException(
            message = "Workspace not found",
            statusCode = Status.NOT_FOUND
          )
        )
      }

      // check project name already exists in this workspace
      existsProject <- projectRepository.existsByName(workspaceId, req.name)

      _ <- if (!existsProject) {
        DBIO.successful(())
      } else {
        DBIO.failed(
          AppException(
            message = s"Project name '${req.name}' already exists",
            statusCode = Status.CONFLICT
          )
        )
      }

      // create project
      projectId <- {
        val newProject = Project(
          name = req.name,
          visibility =
            ProjectVisibility.withName(req.visibility.getOrElse("workspace")),
          workspaceId = workspaceId,
          createdBy = Some(createdBy),
          updatedBy = Some(createdBy)
        )
        projectRepository.createProjectWithOwner(newProject, createdBy)
      }
    } yield projectId

    db.run(action.transactionally)
  }

  def getProjectsByWorkspaceAndUser(
    workspaceId: Int,
    userId: Int
  ): Future[Seq[ProjectSummariesResponse]] = {

    val action = for {
      existsUserInActiveWorkspace <- workspaceRepository
        .isUserInActiveWorkspace(workspaceId, userId)

      projects <- if (existsUserInActiveWorkspace) {
        projectRepository.findNonDeletedByWorkspace(workspaceId)
      } else {
        DBIO.failed(
          AppException(
            message = "Workspace not found",
            statusCode = Status.NOT_FOUND
          )
        )
      }
    } yield projects

    db.run(action)
  }

  def completeProject(projectId: Int, userId: Int): Future[Int] =
    changeStatusIfOwner(
      projectId,
      userId,
      validFrom = Set(ProjectStatus.active),
      next = ProjectStatus.completed,
      errorMsg = "Only active projects can be completed"
    )

  private def changeStatusIfOwner(projectId: Int,
                                  userId: Int,
                                  validFrom: Set[ProjectStatus],
                                  next: ProjectStatus,
                                  errorMsg: String): Future[Int] = {
    val action = for {
      maybeStatus <- projectRepository.findStatusIfOwner(projectId, userId)
      updatedRows <- maybeStatus match {
        case Some(s) if validFrom.contains(s) =>
          projectRepository.updateStatus(projectId, next)
        case Some(_) =>
          DBIO.failed(AppException(errorMsg, Status.BAD_REQUEST))
        case None =>
          DBIO.failed(
            AppException(
              "Project not found or you are not the owner",
              Status.NOT_FOUND
            )
          )
      }
    } yield updatedRows

    db.run(action.transactionally)
  }

  def deleteProject(projectId: Int, userId: Int): Future[Int] =
    changeStatusIfOwner(
      projectId,
      userId,
      validFrom = Set(ProjectStatus.completed),
      next = ProjectStatus.deleted,
      errorMsg = "Only completed projects can be deleted"
    )

  def reopenProject(projectId: Int, userId: Int): Future[Int] =
    changeStatusIfOwner(
      projectId,
      userId,
      validFrom = Set(ProjectStatus.completed, ProjectStatus.deleted),
      next = ProjectStatus.active,
      errorMsg = "Only completed or deleted projects can be reopened"
    )

  def getCompletedProjectsByUserId(
    userId: Int
  ): Future[Seq[dto.response.project.CompletedProjectSummariesResponse]] = {
    db.run(projectRepository.findCompletedProjectsByUserId(userId))
  }

  def getAllMembersInProject(
    projectId: Int,
    userId: Int
  ): Future[Seq[UserInProjectResponse]] = {
    val action = for {
      _ <- ensureUserInActiveProject(userId, projectId)
      members <- projectRepository.getAllMembersInProject(projectId)
    } yield members
    db.run(action)
  }

  private def ensureUserInActiveProject(userId: Int,
                                        projectId: Int): DBIO[Unit] =
    projectRepository.isUserInActiveProject(userId, projectId).flatMap {
      case true => DBIO.successful(())
      case false =>
        DBIO.failed(AppException("Project not found", Status.NOT_FOUND))
    }

  def getProjectDetailById(
    projectId: Int,
    userId: Int
  ): Future[Option[ProjectDetailResponse]] = {
    val action = for {
      _ <- ensureUserInActiveProject(userId, projectId)
      projectOpt <- projectRepository.findProjectBasicInfo(projectId)
      resultOpt <- projectOpt match {
        case Some((id, name, status)) =>
          for {
            columns <- columnRepository.findActiveColumnsByProject(projectId)
            members <- projectRepository.getAllMembersInProject(projectId)
            rankedTasks <- taskRepository.findLimitedActiveTasksByProject(
              projectId
            )
            allUserTasks <- taskRepository.findUserTaskByTaskIds(
              rankedTasks.map(_._1).toSet
            )
          } yield
            Some(
              ProjectDetailResponse.build(
                id,
                name,
                status,
                columns,
                members,
                rankedTasks,
                allUserTasks
              )
            )

        case None =>
          DBIO.successful(None)
      }
    } yield resultOpt

    db.run(action.transactionally)
  }

  def isUserInActiveProject(userId: Int, projectId: Int): Future[Boolean] = {
    db.run(projectRepository.isUserInActiveProject(userId, projectId))
  }

  def getProjectsByUserId(userId: Int): Future[Seq[ProjectResponse]] = {
    db.run(projectRepository.getProjectsByUser(userId)).map {
      _.map(ProjectMapper.toProjectResponse)
    }
  }

  def exportProject(projectId: Int, userId: Int): Future[JsValue] = {
    val action = for {
      projectOpt <- projectRepository.findAccessibleProject(userId, projectId)
      project <- projectOpt match {
        case Some(p) => DBIO.successful(p)
        case None =>
          DBIO.failed(AppException("Project not found", Status.NOT_FOUND))
      }
      columns <- columnRepository.findByProjectId(projectId)
      tasks <- taskRepository.findByProjectId(projectId)
      taskIds = tasks.flatMap(_.id)
      userTasks <- if (taskIds.nonEmpty) {
        userRepository.findUserTasksByTaskIds(taskIds)
      } else { DBIO.successful(Seq.empty) }

      userProjects <- userRepository.findUsersInProjectByProjectId(projectId)
      userIds = (userTasks.map(_.assignedTo) ++ userProjects.map(_.userId)).distinct
      users <- if (userIds.nonEmpty) { userRepository.findPublicByIds(userIds) } else {
        DBIO.successful(Seq.empty)
      }

    } yield
      Json.obj(
        "project" -> Json.toJson(project),
        "columns" -> Json.toJson(columns),
        "tasks" -> Json.toJson(tasks),
        "userTasks" -> Json.toJson(userTasks),
        "userProjects" -> Json.toJson(userProjects),
        "users" -> Json.toJson(users)
      )

    db.run(action)
  }

  def importProject(projectData: ImportProjectData,
                    userId: Int,
                    workspaceId: Int): Future[ProjectResponse] = {
    val action = for {
      exists <- workspaceRepository.isUserInActiveWorkspace(workspaceId, userId)
      _ <- if (exists) { DBIO.successful(()) }
      else { DBIO.failed(AppException("Workspace not found", Status.NOT_FOUND)) }

      projectId <- {
        val newProject = Project(
          name = projectData.project.name,
          visibility = projectData.project.visibility,
          workspaceId = workspaceId,
          createdBy = Some(userId),
          updatedBy = Some(userId)
        )
        projectRepository.importProjectWithOwner(newProject, userId)
      }

      insertedColumnIds <- columnRepository.importColumnBatch(
        projectData.columns.map(_.copy(projectId = projectId, id = None))
      )
      columnIdMap = projectData.columns.map(_.id).zip(insertedColumnIds).toMap
      remappedTasks = projectData.tasks.map(
        t =>
          t.copy(
            id = None,
            columnId = columnIdMap.getOrElse(Some(t.columnId), 0)
        )
      )
      insertedTasks <- taskRepository.importTaskBatch(remappedTasks)
      taskIdMap = projectData.tasks.map(_.id).zip(insertedTasks).toMap

      _ <- projectRepository.importUserBatchIntoProject(
        projectData.userProjects.map(_.copy(id = None, projectId = projectId))
      )

      remappedUserTasks = projectData.userTasks.map(
        u => u.copy(id = None, taskId = taskIdMap.getOrElse(Some(u.taskId), 0))
      )
      _ <- taskRepository.importUserBatchIntoTask(remappedUserTasks)

    } yield {
      ProjectResponse(
        projectId,
        projectData.project.name,
        projectData.project.status
      )
    }

    db.run(action.transactionally)
  }

}
