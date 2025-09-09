package models.entities

import models.Enums.UserWorkspaceRole.UserWorkspaceRole
import models.Enums.UserWorkspaceStatus.UserWorkspaceStatus
import models.Enums.{UserWorkspaceStatus, WorkspaceStatus}
import models.Enums.WorkspaceStatus.WorkspaceStatus

import java.time.{Instant, LocalDateTime}

case class Workspace(
                        id: Option[Int] = None,
                        name: String,
                        description: Option[String] = None,
                        status: WorkspaceStatus = WorkspaceStatus.active,
                        createdBy: Option[Int] = None,
                        createdAt: Option[Instant] = None,
                        updatedAt: Option[Instant] = None,
                        updatedBy: Option[Int] = None,
                        isDeleted: Boolean = false
                    )

case class UserWorkspace(
                          id: Option[Int] = None,
                          userId: Option[Int] = None,
                          workspaceId: Option[Int] = None,
                          role: Option[UserWorkspaceRole] = None,
                          status: UserWorkspaceStatus = UserWorkspaceStatus.active,
                          invitedBy: Option[Int] = None,
                          joinedAt: Option[Instant] = None
                        )