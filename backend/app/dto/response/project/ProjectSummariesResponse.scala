package dto.response.project

import play.api.libs.json._

case class ProjectSummariesResponse(id: Int, name: String)

object ProjectSummariesResponse {
  implicit val format: Format[ProjectSummariesResponse] = Json.format[ProjectSummariesResponse]
}
