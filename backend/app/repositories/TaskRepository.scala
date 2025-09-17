package repositories

import db.MyPostgresProfile.api.{columnStatusTypeMapper, projectStatusTypeMapper, taskStatusTypeMapper}
import dto.response.task.TaskSummaryResponse
import models.Enums.{ColumnStatus, ProjectStatus, TaskStatus}
import models.entities.{Project, Task}
import models.tables.TaskTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import models.tables.TableRegistry.{columns, projects, tasks, userProjects}

@Singleton
class TaskRepository@Inject()(
                               protected val dbConfigProvider: DatabaseConfigProvider
                             )(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def create(task: Task): DBIO[Int] =
    tasks returning tasks.map(_.id) += task

  def existsByPositionAndActiveTrueInColumn(position: Int, columnId: Int): DBIO[Boolean] = {
    tasks
      .filter(t => t.position === position && t.columnId === columnId && t.status === TaskStatus.active)
      .exists
      .result
  }

  def findTaskAndProjectIdIfUserInProject(taskId: Int, userId: Int): DBIO[Option[(Task, Int)]] = {
    val query = for {
      (((t, c), p), up) <- tasks
        .join(columns).on(_.columnId === _.id)
        .join(projects).on { case ((t, c), p) => c.projectId === p.id }
        .join(userProjects).on { case (((t, c), p), up) => p.id === up.projectId }
      if t.id === taskId &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active &&
        up.userId === userId
    } yield (t, p.id)

    query.result.headOption
  }


  def update(task: Task): DBIO[Int] = {
    tasks.filter(_.id === task.id).update(task)
  }

  def findArchivedTasksByProjectId(projectId: Int): DBIO[Seq[TaskSummaryResponse]] = {
    val query = for {
      ((t, c), p) <- tasks
        .join(columns).on(_.columnId === _.id)
        .join(projects).on { case ((t, c), p) => c.projectId === p.id }
      if p.id === projectId &&
        t.status === TaskStatus.archived &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active
    } yield (t.id, t.name, t.position, t.updatedAt)

    query.sortBy(_._4.desc.nullsLast).result.map { rows =>
      rows.map { case (id, name, position, _) =>
        TaskSummaryResponse(id, name, position.get)
      }
    }
  }


}
