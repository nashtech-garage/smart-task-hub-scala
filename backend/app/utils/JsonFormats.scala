package utils

import models.entities.{Column, Project, Task, UserProject, UserTask}
import play.api.libs.json.{Json, OFormat}

object JsonFormats {
  implicit val projectFormat: OFormat[Project] = Json.format[Project]
  implicit val columnFormat: OFormat[Column] = Json.format[Column]
  implicit val taskFormat: OFormat[Task] = Json.format[Task]
  implicit val userTaskFormat: OFormat[UserTask] = Json.format[UserTask]
  implicit val userProjectFormat: OFormat[UserProject] = Json.format[UserProject]
}
