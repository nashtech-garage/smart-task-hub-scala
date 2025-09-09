package models.tables

import models.entities.Project
import db.MyPostgresProfile.api._
import dto.response.project.ProjectSummariesResponse
import models.Enums.ProjectStatus.ProjectStatus
import models.Enums.ProjectVisibility.ProjectVisibility
import slick.lifted.Tag

import java.time.{Instant, LocalDateTime}

class ProjectTable(tag: Tag) extends Table[Project](tag, "projects") {

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def workspaceId = column[Int]("workspace_id")
  def status = column[ProjectStatus]("status")
  def visibility = column[ProjectVisibility]("visibility")
  def createdBy = column[Option[Int]]("created_by")
  def updatedBy = column[Option[Int]]("updated_by")
  def createdAt = column[Instant]("created_at")
  def updatedAt = column[Instant]("updated_at")

  def * = (id.?, name, workspaceId, status, visibility, createdBy, updatedBy, createdAt, updatedAt) <> ((Project.apply _).tupled, Project.unapply)

  def workspaceIdIndex = index("projects_workspace_id_index", workspaceId)
  def createdByIndex = index("projects_created_by_index", createdBy)
  def statusIndex = index("projects_status_index", status)

  def workspaceFk = foreignKey("projects_workspace_id_fkey", workspaceId, TableQuery[WorkspaceTable])(_.id)
  def createdByFk = foreignKey("projects_created_by_fkey", createdBy, TableQuery[UserTable])(_.id.?)
  def updatedByFk = foreignKey("projects_updated_by_fkey", updatedBy, TableQuery[UserTable])(_.id.?)

  def summary = (id, name) <> ((ProjectSummariesResponse.apply _).tupled, ProjectSummariesResponse.unapply)
}