package dto.response.task

import play.api.libs.json.{Json, OFormat}

case class TaskSummaryResponse(id: Int, name: String, position: Int, columnId: Int)

object TaskSummaryResponse {
  implicit val taskSummaryFmt: OFormat[TaskSummaryResponse] = Json.format[TaskSummaryResponse]
}
