package dto.response.task

import models.Enums.TaskStatus.TaskStatus
import play.api.libs.json.{Format, Json}

import java.time.Instant

case class TaskSearchResponse(taskId: Int,
                              taskName: String,
                              taskDescription: Option[String],
                              taskStatus: TaskStatus,
                              projectId: Int,
                              projectName: String,
                              columnName: String,
                              updatedAt: Instant)

object TaskSearchResponse {
    implicit val format: Format[TaskSearchResponse] = Json.format[TaskSearchResponse]
}
