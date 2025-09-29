package dto.response.task

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class TaskSearchResponse(taskId: Int,
                              taskName: String,
                              taskDescription: Option[String],
                              projectId: Int,
                              projectName: String,
                              columnName: String,
                              updatedAt: Instant)

object TaskSearchResponse {
    implicit val format: Format[TaskSearchResponse] = Json.format[TaskSearchResponse]
}
