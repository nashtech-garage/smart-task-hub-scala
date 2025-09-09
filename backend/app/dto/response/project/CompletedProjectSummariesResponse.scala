package dto.response.project

import play.api.libs.json._

case class CompletedProjectSummariesResponse(id: Int,
                                             name: String,
                                             workspaceName: String)

object CompletedProjectSummariesResponse {
  implicit val format: Format[CompletedProjectSummariesResponse] =
    Json.format[CompletedProjectSummariesResponse]
}
