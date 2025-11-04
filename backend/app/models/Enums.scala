package models

import play.api.libs.json._

object Enums {

  private def enumFormat[E <: Enumeration](`enum`: E): Format[E#Value] =
    new Format[E#Value] {
      def writes(o: E#Value): JsValue = JsString(o.toString)
      def reads(json: JsValue): JsResult[E#Value] = json match {
        case JsString(s) =>
          try JsSuccess(`enum`.withName(s))
          catch {
            case _: NoSuchElementException =>
              JsError(s"Invalid value: $s in enum ${`enum`.getClass.getSimpleName}")
          }
        case _ => JsError("Expected a string for enum")
      }
    }

  object UserWorkspaceRole extends Enumeration {
    type UserWorkspaceRole = Value
    val admin, member = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object UserProjectRole extends Enumeration {
    type UserProjectRole = Value
    val owner, member = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object UserWorkspaceStatus extends Enumeration {
    type UserWorkspaceStatus = Value
    val pending, active, inactive = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object ProjectStatus extends Enumeration {
    type ProjectStatus = Value
    val active, completed, deleted = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object TaskPriority extends Enumeration {
    type TaskPriority = Value
    val LOW, MEDIUM, HIGH = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object NotificationType extends Enumeration {
    type NotificationType = Value
    val task_assigned, task_completed, deadline_approaching, comment_added, task_moved = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object WorkspaceStatus extends Enumeration {
    type WorkspaceStatus = Value
    val active, archived = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object ProjectVisibility extends Enumeration {
    type ProjectVisibility = Value
    val Private: Value = Value("private")
    val Workspace: Value = Value("workspace")
    val Public: Value = Value("public")
    implicit val format: Format[Value] = enumFormat(this)
  }

  object ColumnStatus extends Enumeration {
    type ColumnStatus = Value
    val active, archived, deleted = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

  object TaskStatus extends Enumeration {
    type TaskStatus = Value
    val active, archived, deleted = Value
    implicit val format: Format[Value] = enumFormat(this)
  }

}
