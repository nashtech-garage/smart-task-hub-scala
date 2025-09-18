package dto.websocket.board

import dto.response.task.TaskDetailResponse
import dto.response.task.AssignMemberToTaskResponse
import play.api.libs.json._
import dto.websocket.OutgoingMessage
import models.Enums.TaskStatus.TaskStatus

sealed trait BoardMessage extends OutgoingMessage

case class ColumnMoved(columnId: Int, newPosition: Int) extends BoardMessage
case class TaskMoved(taskId: Int, fromColumnId: Int, toColumnId: Int, newPosition: Int) extends BoardMessage
case class TaskCreated(taskId: Int, columnId: Int, taskPosition: Int, name: String) extends BoardMessage
case class MemberAssignedToTask(taskId: Int, assignData: AssignMemberToTaskResponse) extends BoardMessage
case class TaskUpdated(taskId: Int, columnId: Int, taskPosition: Int, detail: TaskDetailResponse) extends BoardMessage
case class TaskStatusUpdated(taskId: Int, columnId: Int, taskPosition: Int, name: String, updatedStatus: TaskStatus) extends BoardMessage

object BoardMessage {
  implicit val columnMovedFormat: OFormat[ColumnMoved] = Json.format[ColumnMoved]
  implicit val taskMovedFormat: OFormat[TaskMoved]     = Json.format[TaskMoved]
  implicit val taskCreatedFormat: OFormat[TaskCreated] = Json.format[TaskCreated]
  implicit val memberAssignedToTaskFormat: OFormat[MemberAssignedToTask] = Json.format[MemberAssignedToTask]
  implicit val taskUpdatedFormat: OFormat[TaskUpdated] = Json.format[TaskUpdated]
  implicit val taskStatusUpdated: OFormat[TaskStatusUpdated] = Json.format[TaskStatusUpdated]

  implicit val writes: Writes[BoardMessage] = Writes {
    case cm: ColumnMoved => Json.obj("type" -> "columnMoved") ++ Json.toJsObject(cm)
    case tm: TaskMoved   => Json.obj("type" -> "taskMoved") ++ Json.toJsObject(tm)
    case tc: TaskCreated => Json.obj("type" -> "taskCreated") ++ Json.toJsObject(tc)
    case ma: MemberAssignedToTask => Json.obj("type" -> "memberAssignedToTask") ++ Json.toJsObject(ma)
    case taskCreated: TaskCreated => Json.obj("type" -> "taskCreated") ++ Json.toJsObject(taskCreated)
    case taskUpdated: TaskUpdated => Json.obj("type" -> "taskUpdated") ++ Json.toJsObject(taskUpdated)
    case taskStatusUpdated: TaskStatusUpdated => Json.obj("type" -> "taskStatusUpdated") ++ Json.toJsObject(taskStatusUpdated)
  }
}