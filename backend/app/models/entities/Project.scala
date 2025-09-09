package models.entities

import models.Enums.ColumnStatus.ColumnStatus
import models.Enums.{ColumnStatus, ProjectStatus, ProjectVisibility}
import models.Enums.ProjectStatus.ProjectStatus
import models.Enums.ProjectVisibility.ProjectVisibility

import java.time.{Instant, LocalDateTime}

case class Project(
                    id: Option[Int] = None,
                    name: String,
                    workspaceId: Int,
                    status: ProjectStatus = ProjectStatus.active,
                    visibility: ProjectVisibility = ProjectVisibility.Workspace,
                    createdBy: Option[Int] = None,
                    updatedBy: Option[Int] = None,
                    createdAt: Instant = Instant.now(),
                    updatedAt: Instant = Instant.now()
                  )

case class Column(
                   id: Option[Int] = None,
                   projectId: Int,
                   name: String,
                   position: Int,
                   createdAt: Instant = Instant.now(),
                   updatedAt: Instant = Instant.now(),
                   status: ColumnStatus = ColumnStatus.active
                 )