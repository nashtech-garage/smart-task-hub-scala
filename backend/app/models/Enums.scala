package models

import play.api.libs.json.{
  Format,
  JsError,
  JsResult,
  JsString,
  JsSuccess,
  JsValue
}

object Enums {

  object UserWorkspaceRole extends Enumeration {
    type UserWorkspaceRole = Value
    val admin, member = Value
  }

  object UserProjectRole extends Enumeration {
    type UserProjectRole = Value
    val owner, member = Value
  }

  object UserWorkspaceStatus extends Enumeration {
    type UserWorkspaceStatus = Value
    val pending, active, inactive = Value
  }

  object ProjectStatus extends Enumeration {
    type ProjectStatus = Value
    val active, completed, deleted = Value

    implicit val format: Format[Value] = new Format[Value] {
      def writes(o: Value): JsValue = JsString(o.toString)
      def reads(json: JsValue): JsResult[Value] = json match {
        case JsString(s) =>
          try {
            JsSuccess(ProjectStatus.withName(s))
          } catch {
            case _: NoSuchElementException =>
              JsError(s"Invalid ProjectStatus: $s")
          }
        case _ => JsError("String value expected for ProjectStatus")
      }
    }
  }

  object TaskPriority extends Enumeration {
    type TaskPriority = Value
    val LOW, MEDIUM, HIGH = Value

    implicit val format: Format[Value] = new Format[Value] {
      def writes(o: Value): JsValue = JsString(o.toString)
      def reads(json: JsValue): JsResult[Value] = json match {
        case JsString(s) =>
          try {
            JsSuccess(TaskPriority.withName(s))
          } catch {
            case _: NoSuchElementException =>
              JsError(s"Invalid TaskPriority: $s")
          }
        case _ => JsError("String value expected for TaskPriority")
      }
    }
  }

  object NotificationType extends Enumeration {
    type NotificationType = Value
    val task_assigned, task_completed, deadline_approaching, comment_added,
    task_moved = Value
  }

  object WorkspaceStatus extends Enumeration {
    type WorkspaceStatus = Value
    val active, archived = Value
  }

  object ProjectVisibility extends Enumeration {
    type ProjectVisibility = Value
    val Private: Value = Value("private")
    val Workspace: Value = Value("workspace")
    val Public: Value = Value("public")
  }

  object ColumnStatus extends Enumeration {
    type ColumnStatus = Value
    val active, archived, deleted = Value
    implicit val format: Format[Value] = new Format[Value] {
      def writes(o: Value): JsValue = JsString(o.toString)
      def reads(json: JsValue): JsResult[Value] = json match {
        case JsString(s) =>
          try {
            JsSuccess(ColumnStatus.withName(s))
          } catch {
            case _: NoSuchElementException =>
              JsError(s"Invalid ColumnStatus: $s")
          }
        case _ => JsError("String value expected for ColumnStatus")
      }
    }
  }

  object TaskStatus extends Enumeration {
    type TaskStatus = Value
    val active, archived, deleted = Value

    implicit val format: Format[Value] = new Format[Value] {
      def writes(o: Value): JsValue = JsString(o.toString)
      def reads(json: JsValue): JsResult[Value] = json match {
        case JsString(s) =>
          try {
            JsSuccess(TaskStatus.withName(s))
          } catch {
            case _: NoSuchElementException =>
              JsError(s"Invalid TaskStatus: $s")
          }
        case _ => JsError("String value expected for TaskStatus")
      }
    }
  }

}
