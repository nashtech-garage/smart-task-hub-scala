package repositories

import db.MyPostgresProfile.api.{columnStatusTypeMapper, projectStatusTypeMapper, taskStatusTypeMapper}
import models.Enums.{ColumnStatus, ProjectStatus, TaskStatus}
import models.entities.Task
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

  def findTaskIfUserInProject(taskId: Int, userId: Int): DBIO[Option[Task]] = {
    val query = (for {
      (((t, c), p), up) <- tasks
        .join(columns).on(_.columnId === _.id)
        .join(projects).on { case ((t, c), p) => c.projectId === p.id }
        .join(userProjects).on { case (((t, c), p), up) => p.id === up.projectId }
      if t.id === taskId &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active &&
        up.userId === userId
    } yield t)

    query.result.headOption
  }


  def update(task: Task): DBIO[Int] = {
    tasks.filter(_.id === task.id).update(task)
  }


}
