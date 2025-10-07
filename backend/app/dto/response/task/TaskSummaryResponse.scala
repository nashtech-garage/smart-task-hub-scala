package dto.response.task

import play.api.libs.json.{Json, OFormat}

import java.time.Instant

case class TaskSummaryResponse(id: Int, name: String, position: Int, columnId: Int, memberIds: Seq[Int], updatedAt: Instant)

object TaskSummaryResponse {
  implicit val taskSummaryFmt: OFormat[TaskSummaryResponse] = Json.format[TaskSummaryResponse]
}
