package models.entities

import models.Enums.ColumnStatus.ColumnStatus
import models.Enums.ProjectStatus.ProjectStatus
import models.Enums.ProjectVisibility.ProjectVisibility
import models.Enums.{ColumnStatus, ProjectStatus, ProjectVisibility}
import play.api.libs.json.{Json, OFormat}

import java.time.Instant

case class Project(id: Option[Int] = None,
                   name: String,
                   workspaceId: Int,
                   status: ProjectStatus = ProjectStatus.active,
                   visibility: ProjectVisibility = ProjectVisibility.Workspace,
                   createdBy: Option[Int] = None,
                   updatedBy: Option[Int] = None,
                   createdAt: Instant = Instant.now(),
                   updatedAt: Instant = Instant.now())
object Project {
  implicit val projectFormat: OFormat[Project] = Json.format[Project]
}

case class Column(id: Option[Int] = None,
                  projectId: Int,
                  name: String,
                  position: Int,
                  createdAt: Instant = Instant.now(),
                  updatedAt: Instant = Instant.now(),
                  status: ColumnStatus = ColumnStatus.active)
object Column {
  implicit val columnFormat: OFormat[Column] = Json.format[Column]
}
