package models.entities

import models.Enums.TaskPriority.TaskPriority
import models.Enums.TaskStatus
import models.Enums.TaskStatus.TaskStatus

import java.time.{Instant, LocalDateTime}

case class Task(
                 id: Option[Int] = None,
                 columnId: Int,
                 name: String,
                 description: Option[String] = None,
                 startDate: Option[Instant] = None,
                 endDate: Option[Instant] = None,
                 priority: Option[TaskPriority] = None,
                 position: Option[Int] = None,
                 createdBy: Option[Int] = None,
                 updatedBy: Option[Int] = None,
                 createdAt: Instant = Instant.now(),
                 updatedAt: Instant = Instant.now(),
                 status: TaskStatus = TaskStatus.active,
                 isCompleted: Boolean = false
               )

case class UserTask(
                     id: Option[Int] = None,
                     taskId: Int,
                     assignedTo: Int,
                     assignedBy: Option[Int] = None,
                     assignedAt: Instant = Instant.now()
                   )

case class Checklist(
                      id: Option[Int] = None,
                      taskId: Option[Int] = None,
                      name: Option[String] = None,
                      createdAt: Option[LocalDateTime] = None,
                      updatedAt: Option[LocalDateTime] = None
                    )

case class ChecklistItem(
                          id: Option[Int] = None,
                          checklistId: Option[Int] = None,
                          content: Option[String] = None,
                          isCompleted: Boolean = false,
                          createdAt: Option[LocalDateTime] = None,
                          updatedAt: Option[LocalDateTime] = None
                        )

case class TaskComment(
                        id: Option[Int] = None,
                        taskId: Option[Int] = None,
                        userId: Option[Int] = None,
                        content: Option[String] = None,
                        createdAt: Option[LocalDateTime] = None,
                        updatedAt: Option[LocalDateTime] = None
                      )

case class Tag(
                id: Option[Int] = None,
                projectId: Option[Int] = None,
                name: Option[String] = None,
                color: Option[String] = None,
                createdAt: Option[LocalDateTime] = None
              )

case class TaskTag(
                    id: Option[Int] = None,
                    taskId: Option[Int] = None,
                    tagId: Option[Int] = None
                  )
