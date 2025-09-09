package dto.response.workspace

import play.api.libs.json.{Format, Json}

case class WorkspaceResponse (
    id: Int,
    name: String,
    desc: Option[String]
)

object WorkspaceResponse {
    implicit val format: Format[WorkspaceResponse] = Json.format[WorkspaceResponse]
}