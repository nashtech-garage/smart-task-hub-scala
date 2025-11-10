package models.tables

import slick.jdbc.PostgresProfile.api._

object TableRegistry {
  lazy val roles = TableQuery[RoleTable]
  lazy val users = TableQuery[UserTable]
  lazy val workspaces = TableQuery[WorkspaceTable]
  lazy val userWorkspaces = TableQuery[UserWorkspaceTable]
  lazy val projects = TableQuery[ProjectTable]
  lazy val userProjects = TableQuery[UserProjectTable]
  lazy val columns = TableQuery[ColumnTable]
  lazy val tasks = TableQuery[TaskTable]
  lazy val checklists = TableQuery[ChecklistTable]
  lazy val checklistItems = TableQuery[ChecklistItemTable]
  lazy val taskComments = TableQuery[TaskCommentTable]
  lazy val tags = TableQuery[TagTable]
  lazy val taskTags = TableQuery[TaskTagTable]
  lazy val notifications = TableQuery[NotificationTable]
  lazy val activityLogs = TableQuery[ActivityLogTable]
  lazy val userTasks = TableQuery[UserTaskTable]
  lazy val userProfiles = TableQuery[UserProfileTable]

  // All tables for schema creation/evolution
  val allTables = Seq(
    roles, users, workspaces, userWorkspaces, projects, columns,
    tasks, checklists, checklistItems, taskComments, tags, taskTags,
    notifications, activityLogs
  )
}
