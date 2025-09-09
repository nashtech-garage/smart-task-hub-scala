package models.entities

import models.Enums.UserProjectRole
import models.Enums.UserProjectRole.UserProjectRole

import java.time.{Instant, LocalDateTime}

case class Role(id: Option[Int] = None, name: String)

case class User(
    id: Option[Int] = None,
    name: String,
    email: String,
    password: String,
    avatarUrl: Option[String] = None,
    roleId: Option[Int] = None,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now()
)

case class UserProject(id: Option[Int] = None,
                       userId: Int,
                       projectId: Int,
                       role: UserProjectRole = UserProjectRole.member,
                       invitedBy: Option[Int] = None,
                       joinedAt: Instant = Instant.now())