package mappers

import dto.response.task.{AssignedMemberResponse, TaskDetailResponse}
import models.entities.Task

object TaskMapper {

  def toDetailResponse(entity: Task): TaskDetailResponse = {
    TaskDetailResponse(
      id = entity.id.getOrElse(0),
      name = entity.name,
      description = entity.description,
      startDate = entity.startDate,
      endDate = entity.endDate,
      priority = entity.priority.map(_.toString),
      status = entity.status.toString,
      position = entity.position.getOrElse(0),
      columnId = entity.columnId,
      isCompleted = entity.isCompleted,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt
    )
  }
  def toDetailWithAssignMembersResponse(entity: Task,assignedMembers: Seq[AssignedMemberResponse]): TaskDetailResponse = {
    TaskDetailResponse(
      id = entity.id.getOrElse(0),
      name = entity.name,
      description = entity.description,
      startDate = entity.startDate,
      endDate = entity.endDate,
      priority = entity.priority.map(_.toString),
      status = entity.status.toString,
      position = entity.position.getOrElse(0),
      columnId = entity.columnId,
      isCompleted = entity.isCompleted,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt,
      assignedMembers = assignedMembers
    )
  }

}
