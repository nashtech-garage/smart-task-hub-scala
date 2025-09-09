package repositories

import db.MyPostgresProfile.api.{projectStatusTypeMapper, userProjectRoleTypeMapper}
import dto.response.project.{CompletedProjectSummariesResponse, ProjectResponse, ProjectSummariesResponse}
import dto.response.user.UserInProjectResponse
import models.Enums.ProjectStatus.ProjectStatus
import models.Enums.{ProjectStatus, UserProjectRole}
import models.entities.{Column, Project, UserProject}
import models.tables.TableRegistry.{columns, users}
import models.tables.{ProjectTable, UserProjectTable, WorkspaceTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ProjectRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val projects = TableQuery[ProjectTable]
  private val userProjects = TableQuery[UserProjectTable]
  private val workspaces = TableQuery[WorkspaceTable]

  def createProjectWithOwner(project: Project, ownerId: Int): DBIO[Int] = {
    for {
      projectId <- (projects returning projects.map(_.id)) += project

      _ <- DBIO.seq(
        userProjects += UserProject(
          userId = ownerId,
          projectId = projectId,
          role = UserProjectRole.owner,
          joinedAt = Instant.now()
        ),
        columns ++= Seq(
          Column(projectId = projectId, name = "To Do", position = 1),
          Column(projectId = projectId, name = "In Progress", position = 1000),
          Column(projectId = projectId, name = "Done", position = 2000)
        )
      )
    } yield projectId
  }

  def findNonDeletedByWorkspace(
    workspaceId: Int
  ): DBIO[Seq[ProjectSummariesResponse]] = {
    projects
      .filter(
        p =>
          p.workspaceId === workspaceId && p.status =!= ProjectStatus.deleted && p.status =!= ProjectStatus.completed
      )
      .map(_.summary)
      .result
  }

  def findStatusIfOwner(projectId: Int,
                        userId: Int): DBIO[Option[ProjectStatus]] = {
    (for {
      p <- projects if p.id === projectId
      up <- userProjects
      if up.projectId === p.id && up.userId === userId && up.role === UserProjectRole.owner
    } yield p.status).result.headOption
  }

  def updateStatus(projectId: Int, status: ProjectStatus): DBIO[Int] = {
    projects.filter(_.id === projectId).map(_.status).update(status)
  }

  def findCompletedProjectsByUserId(
    userId: Int
  ): DBIO[Seq[CompletedProjectSummariesResponse]] = {
    (for {
      up <- userProjects
      if up.userId === userId && up.role === UserProjectRole.owner
      p <- projects
      if p.id === up.projectId && p.status === ProjectStatus.completed
      w <- workspaces if w.id === p.workspaceId && !w.isDeleted
    } yield (p.id, p.name, w.name)).result
      .map(_.map((CompletedProjectSummariesResponse.apply _).tupled))
  }

  def isUserInActiveProject(userId: Int, projectId: Int): DBIO[Boolean] = {
    (for {
      up <- userProjects if up.userId === userId && up.projectId === projectId
      p <- projects
      if p.id === up.projectId && p.status === ProjectStatus.active
    } yield up).exists.result
  }

  def isUserInProject(userId: Int, projectId: Int): DBIO[Boolean] = {
    (for {
      up <- userProjects if up.userId === userId && up.projectId === projectId
    } yield up).exists.result
  }

  def getAllMembersInProject(projectId: Int): DBIO[Seq[UserInProjectResponse]] = {
    (for {
      up <- userProjects if up.projectId === projectId
      u <- users if u.id === up.userId
    } yield (u.id, u.name)).result
      .map(_.map((UserInProjectResponse.apply _).tupled))
  }

  def findById(projectId: Int): DBIO[Option[ProjectResponse]] = {
    projects
      .filter(_.id === projectId)
      .map(p => (p.id, p.name, p.status))
      .result
      .headOption
      .map(_.map { case (id, name, status) =>
        ProjectResponse(id, name, status)
      })
  }
}
