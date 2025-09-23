package dto.response.column

import dto.response.task.TaskSummaryResponse
import play.api.libs.json.{Json, OFormat}

case class ColumnWithTasksResponse(id: Int,
                                   name: String,
                                   position: Int,
                                   taskIds: Seq[Int])
object ColumnWithTasksResponse {
  implicit val columnWithTasksFmt: OFormat[ColumnWithTasksResponse] =
    Json.format[ColumnWithTasksResponse]
}
