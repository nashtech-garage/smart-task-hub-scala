package dto.response.project

import models.Enums.ProjectStatus.ProjectStatus
import play.api.libs.json.{Format, Json}

case class ProjectResponse (id: Int, name: String, status: ProjectStatus)

object ProjectResponse {
  implicit val format: Format[ProjectResponse] = Json.format[ProjectResponse]
}