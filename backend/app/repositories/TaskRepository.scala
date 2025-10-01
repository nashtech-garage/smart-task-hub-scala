package repositories

import db.MyPostgresProfile.api.{columnStatusTypeMapper, projectStatusTypeMapper, taskStatusTypeMapper}
import dto.response.task.{AssignMemberToTaskResponse, AssignedMemberResponse, TaskSummaryResponse}
import dto.response.task.AssignMemberToTaskResponse
import dto.response.task.TaskSummaryResponse
import models.Enums.TaskStatus.TaskStatus
import models.Enums.{ColumnStatus, ProjectStatus, TaskStatus}
import models.entities.{Task, UserTask}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.tables.TableRegistry.{columns, projects, tasks, userProjects, userTasks, users}

import java.time.Instant

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

  def findAssignedMembers(taskId: Int): DBIO[Seq[AssignedMemberResponse]] = {
    val query = for {
      ut <- userTasks if ut.taskId === taskId
      u  <- users if u.id === ut.assignedTo
    } yield (u.id, u.name)

    query.result.map(_.map { case (id, name) => AssignedMemberResponse(id, name) })
  }

  def update(task: Task): DBIO[Int] = {
    tasks.filter(_.id === task.id).update(task)
  }

  def assignMemberToTask(taskId: Int, userId: Int, assignedBy: Option[Int]): DBIO[Int] = {
    val userTask = UserTask(
      taskId = taskId,
      assignedTo = userId,
      assignedBy = assignedBy
    )
    userTasks += userTask
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
    } yield (t.id, t.name, t.position, t.columnId, t.updatedAt)

    query.sortBy(_._5.desc.nullsLast).result.map { rows =>
      rows.map { case (id, name, position, columnId, _) =>
        TaskSummaryResponse(id, name, position.get, columnId)
      }
    }
  }

  def findUserInProjectNotAssigned(userId: Int, taskId: Int): DBIO[Option[AssignMemberToTaskResponse]] = {
    val query = for {
      t <- tasks if t.id === taskId
      c <- columns if c.id === t.columnId
      up <- userProjects if up.userId === userId && up.projectId === c.projectId
      u  <- users if u.id === userId
      if !(userTasks.filter(ut => ut.taskId === taskId && ut.assignedTo === userId)).exists
    } yield (u.id, u.name, c.id)

    query.result.headOption.map(_.map { case (uid, uname, colId) =>
      AssignMemberToTaskResponse(uid, uname, colId)
    })
  }

  def findActiveTaskByProjectId(projectId: Int): DBIO[Seq[TaskSummaryResponse]] = {
    val query = for {
      ((t, c), p) <- tasks
        .join(columns).on(_.columnId === _.id)
        .join(projects).on { case ((t, c), p) => c.projectId === p.id }
      if p.id === projectId &&
        t.status === TaskStatus.active &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active
    } yield (t.id, t.name, t.position, t.columnId, t.updatedAt)

    query.sortBy(_._4.asc.nullsLast).result.map { rows =>
      rows.map { case (id, name, position, columnId, _) =>
        TaskSummaryResponse(id, name, position.get, columnId)
      }
    }
  }

  def insertTaskBatch(tasksChunk: Seq[Task]): Future[Seq[Int]] = {
    db.run((tasks returning tasks.map(_.id)) ++= tasksChunk).map(_.toSeq)
  }

  def insertUserBatchIntoTask(entries: Seq[UserTask]): Future[Unit] = {
    val insertQuery = userTasks ++= entries
    db.run(insertQuery).map(_ => ())
  }

  def search(
              projectIds: Option[Seq[Int]] = None,
              keyword: Option[String] = None,
              userId: Int
            ): Query[(Rep[Int],
    Rep[String],
    Rep[Option[String]],
    Rep[TaskStatus],
    Rep[Int],
    Rep[String],
    Rep[String],
    Rep[Instant]),
    (Int, String, Option[String], TaskStatus, Int, String, String, Instant),
    Seq] = {

    val baseQuery =
      for {
        (((t, c), p), up) <- tasks
          .join(columns)
          .on(_.columnId === _.id)
          .join(projects)
          .on(_._2.projectId === _.id)
          .join(userProjects)
          .on(_._2.id === _.projectId)
        if up.userId === userId && t.status =!= TaskStatus.deleted && p.status =!= ProjectStatus.deleted && c.status =!= ColumnStatus.deleted
      } yield (t, c, p)

    val filtered = baseQuery
      .filterOpt(projectIds.filter(_.nonEmpty)) {
        case ((_, _, p), ids) =>
          p.id inSet ids
      }
      .filterOpt(keyword.filter(_.nonEmpty)) {
        case ((t, _, _), kw) =>
          t.name.toLowerCase like s"%${kw.toLowerCase}%"
      }

    filtered
      .sortBy { case (t, _, _) => t.updatedAt.desc }
      .map {
        case (t, c, p) =>
          (t.id, t.name, t.description, t.status, p.id, p.name, c.name, t.updatedAt)
      }
  }

}
