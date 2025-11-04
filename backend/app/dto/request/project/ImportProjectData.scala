package dto.request.project

import models.entities.{Column, Project, Task, UserProject, UserTask}
import play.api.libs.json.{Json, OFormat}

case class ImportProjectData(
                       project: Project,
                       columns: Seq[Column],
                       tasks: Seq[Task],
                       userTasks: Seq[UserTask],
                       userProjects: Seq[UserProject]
                     )
object ImportProjectData {
  implicit val format: OFormat[ImportProjectData] = Json.format[ImportProjectData]
}