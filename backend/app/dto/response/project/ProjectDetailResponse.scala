package dto.response.project

import dto.response.column.ColumnWithTasksResponse
import dto.response.task.TaskSummaryResponse
import dto.response.user.UserInProjectResponse
import models.Enums.ProjectStatus.ProjectStatus
import models.entities.{Column, UserTask}
import play.api.libs.json.{Format, Json}

case class ProjectDetailResponse(id: Int,
                                 name: String,
                                 status: ProjectStatus,
                                 columns: Seq[ColumnWithTasksResponse],
                                 tasks: Seq[TaskSummaryResponse],
                                 members: Seq[UserInProjectResponse])

object ProjectDetailResponse {
  implicit val format: Format[ProjectDetailResponse] =
    Json.format[ProjectDetailResponse]

  def build(
             id: Int,
             name: String,
             status: ProjectStatus,
             columns: Seq[Column],
             members: Seq[UserInProjectResponse],
             rankedTasks: Seq[(Int, String, Int, Int, java.time.Instant, Int, Int)],
             allUserTasks: Seq[UserTask]
           ): ProjectDetailResponse = {
    val memberIdsByTask = allUserTasks
      .groupBy(_.taskId)
      .view
      .mapValues(_.map(_.assignedTo).distinct)
      .toMap

    val allTasks = rankedTasks.map {
      case (taskId, taskName, pos, colId, updatedAt, _, totalTasksInColumn) =>
        (taskId, taskName, pos, colId, updatedAt, totalTasksInColumn)
    }

    val totalTasksByColumn =
      allTasks.groupBy(_._4).view.mapValues(_.head._6).toMap

    val tasksByColumn =
      allTasks.groupBy(_._4).view.mapValues { tasks =>
        tasks.map {
          case (taskId, taskName, pos, colId, updatedAt, _) =>
            TaskSummaryResponse(
              taskId,
              taskName,
              pos,
              colId,
              memberIdsByTask.getOrElse(taskId, Seq.empty),
              updatedAt
            )
        }
      }.toMap

    val columnsResponse = columns.map { col =>
      val colId = col.id.get
      val taskIds = tasksByColumn.getOrElse(colId, Seq.empty).map(_.id)
      val totalTasks = totalTasksByColumn.getOrElse(colId, 0)

      ColumnWithTasksResponse(colId, col.name, col.position, taskIds, totalTasks)
    }

    val tasksResponse =
      tasksByColumn.values.flatten.toSeq.sortBy(_.position)

    ProjectDetailResponse(id, name, status, columnsResponse, tasksResponse, members)
  }
}
