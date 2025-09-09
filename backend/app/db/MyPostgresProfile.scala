package db

import com.github.tminglei.slickpg._
import models.Enums._
import slick.jdbc.{JdbcType, PostgresProfile}

/**
  * MyPostgresProfile is a custom Slick profile for working with PostgreSQL,
  * extending `PostgresProfile` and integrating `PgEnumSupport` from slick-pg.
  *
  * Purpose:
  *   - Allows Slick to automatically map Scala Enumerations to PostgreSQL enum types.
  *   - Makes table definitions cleaner because the mappers are pre-defined.
  *
  * What is a Slick Profile?
  *
  * In Slick, a Profile defines the API and behavior for interacting with a database.
  * Slick provides default profiles like `H2Profile`, `PostgresProfile`, etc.
  * You can create a custom profile to:
  *   - Add extensions (e.g., PgEnumSupport, PgPlayJsonSupport, arrays, etc.)
  *   - Declare implicit mappers for special types (enums, JSON, arrays, etc.)
  *
  * In this file:
  *   - We extend `PostgresProfile` → inherit all default Slick features for Postgres.
  *   - We mix in `PgEnumSupport` → adds the ability to map Scala enums to Postgres enums.
  *   - The `API` trait defines implicit mappers for all project enums.
  *
  * Usage:
  *
  * 1. Define the PostgreSQL enum type in the database:
  *    {{{CREATE TYPE workspace_status AS ENUM ('active', 'archived'); }}}
  *
  * 2. Define the corresponding Scala Enumeration:
  *    {{{object WorkspaceStatus extends Enumeration {
  *      type WorkspaceStatus = Value
  *      val active, archived = Value
  *    } }}}
  *
  * 3. Import MyPostgresProfile in your table definition file:
  *    {{{ import db.MyPostgresProfile.api._ }}}
  *
  * 4. Declare a column using the enum:
  *    {{{ def status = column[WorkspaceStatus]("status") }}}
  *
  * 5. When Slick runs queries, it automatically converts between Scala Enumeration and PostgreSQL enum.
  */
trait MyPostgresProfile extends PostgresProfile with PgEnumSupport {

  /**
    * Override `api` to return a custom API instance.
    * Whenever you import `MyPostgresProfile.api._`, all the implicit mappers below will be available.
    */
  override val api: API = new API {}

  /**
    * Custom API containing implicit mappers for PostgreSQL enums.
    * Each mapper uses slick-pg's `createEnumJdbcType` to map Scala Enumeration ↔ Postgres enum.
    */
  trait API extends JdbcAPI {

    /** Mapper for enum workspace_status */
    implicit val workspaceStatusTypeMapper: JdbcType[WorkspaceStatus.Value] =
      createEnumJdbcType("workspace_status", WorkspaceStatus)

    /** Mapper for enum notification_type */
    implicit val notificationTypeTypeMapper: JdbcType[NotificationType.Value] =
      createEnumJdbcType("notification_type", NotificationType)

    /** Mapper for enum project_status */
    implicit val projectStatusTypeMapper: JdbcType[ProjectStatus.Value] =
      createEnumJdbcType("project_status", ProjectStatus)

    /** Mapper for enum task_priority */
    implicit val taskPriorityTypeMapper: JdbcType[TaskPriority.Value] =
      createEnumJdbcType("task_priority", TaskPriority)

    /** Mapper for enum user_workspace_role */
    implicit val userWorkspaceRoleTypeMapper
      : JdbcType[UserWorkspaceRole.Value] =
      createEnumJdbcType("user_workspace_role", UserWorkspaceRole)

    /** Mapper for enum user_workspace_status */
    implicit val userWorkspaceStatusTypeMapper
      : JdbcType[UserWorkspaceStatus.Value] =
      createEnumJdbcType("user_workspace_status", UserWorkspaceStatus)

    implicit val workspaceVisibilityTypeMapper
      : JdbcType[ProjectVisibility.Value] =
      createEnumJdbcType("project_visibility", ProjectVisibility)

    implicit val userProjectRoleTypeMapper: JdbcType[UserProjectRole.Value] =
      createEnumJdbcType("user_project_role", UserProjectRole)

    implicit val columnStatusTypeMapper: JdbcType[ColumnStatus.Value] =
      createEnumJdbcType("column_status", ColumnStatus)

    implicit val taskStatusTypeMapper: JdbcType[TaskStatus.Value] =
      createEnumJdbcType("task_status", TaskStatus)
  }
}

/**
  * Singleton object of the custom profile.
  * By importing `MyPostgresProfile.api._`, you get:
  *   - All default API of PostgresProfile
  *   - Implicit enum mappers defined above
  */
object MyPostgresProfile extends MyPostgresProfile
