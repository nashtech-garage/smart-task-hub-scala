package dto.websocket.board

object BoardMessageTypes {
  final val COLUMN_MOVED: String            = "COLUMN_MOVED"
  final val TASK_MOVED: String              = "TASK_MOVED"
  final val TASK_CREATED: String            = "TASK_CREATED"
  final val MEMBER_ASSIGNED_TO_TASK: String = "MEMBER_ASSIGNED_TO_TASK"
  final val MEMBER_UNASSIGNED_TO_TASK: String = "MEMBER_UNASSIGNED_TO_TASK"
  final val TASK_UPDATED: String            = "TASK_UPDATED"
  final val TASK_STATUS_UPDATED: String     = "TASK_STATUS_UPDATED"
  final val COLUMN_CREATED: String            = "COLUMN_CREATED"
  final val COLUMN_UPDATED: String            = "COLUMN_UPDATED"
  final val COLUMN_STATUS_UPDATED: String     = "COLUMN_STATUS_UPDATED"
}