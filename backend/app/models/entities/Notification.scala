package models.entities

import models.Enums.NotificationType.NotificationType

import java.time.LocalDateTime

case class Notification(
                         id: Option[Int] = None,
                         userId: Option[Int] = None,
                         taskId: Option[Int] = None,
                         `type`: Option[NotificationType] = None,
                         message: Option[String] = None,
                         isRead: Boolean = false,
                         createdAt: Option[LocalDateTime] = None
                       )

case class ActivityLog(
                        id: Option[Int] = None,
                        userId: Option[Int] = None,
                        projectId: Option[Int] = None,
                        taskId: Option[Int] = None,
                        action: Option[String] = None,
                        content: Option[String] = None,
                        createdAt: Option[LocalDateTime] = None
                      )
