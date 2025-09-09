package dto.response.task

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class TaskDetailResponse(id: Int,
                              name: String,
                              description: Option[String],
                              startDate: Option[Instant],
                              endDate: Option[Instant],
                              priority: Option[String],
                              status: String,
                              position: Int,
                              columnId: Int,
                              isCompleted: Boolean,
                              createdAt: Instant,
                              updatedAt: Instant
                             )

object TaskDetailResponse {
  implicit val format: Format[TaskDetailResponse] = Json.format[TaskDetailResponse]
}