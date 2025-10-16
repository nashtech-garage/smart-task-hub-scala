package repositories

import db.MyPostgresProfile.api.{columnStatusTypeMapper, projectStatusTypeMapper, taskStatusTypeMapper}
import dto.response.task.{AssignMemberToTaskResponse, AssignedMemberResponse, TaskSummaryResponse}
import models.Enums.TaskStatus.TaskStatus
import models.Enums.{ColumnStatus, ProjectStatus, TaskStatus}
import models.entities.{Task, UserTask}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

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
        .join(projects).on { case ((_, c), p) => c.projectId === p.id }
        .join(userProjects).on { case (((_, _), p), up) => p.id === up.projectId }
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

  def unassignMemberFromTask(taskId: Int, userId: Int): DBIO[Int] = {
    userTasks.filter(ut => ut.taskId === taskId && ut.assignedTo === userId).delete
  }

  def findArchivedTasksByProjectId(projectId: Int): DBIO[Seq[TaskSummaryResponse]] = {
    val query = for {
      (((t, c), p), utOpt) <- tasks
        .join(columns).on(_.columnId === _.id)
        .join(projects).on { case ((_, c), p) => c.projectId === p.id }
        .joinLeft(userTasks).on { case (((t, _), _), ut) => ut.taskId === t.id }
      if p.id === projectId &&
        t.status === TaskStatus.archived &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active
    } yield (t.id, t.name, t.position, t.columnId, t.updatedAt, utOpt.map(_.assignedTo))

    query.result.map { rows =>
      rows
        .groupBy { case (id, name, pos, colId, updatedAt, _) =>
          (id, name, pos, colId, updatedAt)
        }
        .map { case ((id, name, pos, colId, updatedAt), group) =>
          val memberIds = group.flatMap(_._6).distinct
          TaskSummaryResponse(id, name, pos, colId, memberIds, updatedAt)
        }
        .toSeq
        .sortBy(_.updatedAt).reverse
    }
  }

  def findUserInProjectNotAssigned(userId: Int, taskId: Int): DBIO[Option[AssignMemberToTaskResponse]] = {
    val query = for {
      t <- tasks if t.id === taskId
      c <- columns if c.id === t.columnId
      up <- userProjects if up.userId === userId && up.projectId === c.projectId
      u  <- users if u.id === userId
      if !userTasks.filter(ut => ut.taskId === taskId && ut.assignedTo === userId).exists
    } yield (u.id, u.name, t.id)

    query.result.headOption.map(_.map { case (uid, uname, taskId) =>
      AssignMemberToTaskResponse(uid, uname, taskId)
    })
  }

  def findActiveTaskByProjectId(projectId: Int): DBIO[Seq[TaskSummaryResponse]] = {
    val query = for {
      (((t, c), p), utOpt) <- tasks
        .join(columns).on(_.columnId === _.id)
        .join(projects).on { case ((_, c), p) => c.projectId === p.id }
        .joinLeft(userTasks).on { case (((t, _), _), ut) => ut.taskId === t.id }
      if p.id === projectId &&
        t.status === TaskStatus.active &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active
    } yield (t.id, t.name, t.position, t.columnId, t.updatedAt, utOpt.map(_.assignedTo))

    query.result.map { rows =>
      rows
        .groupBy { case (id, name, pos, colId, updatedAt, _) =>
          (id, name, pos, colId, updatedAt)
        }
        .map { case ((id, name, pos, colId, updatedAt), group) =>
          val memberIds = group.flatMap(_._6).distinct
          TaskSummaryResponse(id, name, pos, colId, memberIds, updatedAt)
        }
        .toSeq
        .sortBy(_.columnId)
    }
  }

  def findActiveTaskByColumnId(
                                columnId: Int,
                                limit: Int,
                                offset: Int
                              ): DBIO[Seq[TaskSummaryResponse]] = {
    val query = for {
      (((t, c), p), utOpt) <- tasks
        .join(columns).on(_.columnId === _.id)
        .join(projects).on { case ((_, c), p) => c.projectId === p.id }
        .joinLeft(userTasks).on { case (((t, _), _), ut) => ut.taskId === t.id }
      if c.id === columnId &&
        t.status === TaskStatus.active &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active
    } yield (t.id, t.name, t.position, t.columnId, t.updatedAt, utOpt.map(_.assignedTo))

    query
      .sortBy(_._3) // sort by position (t.position)
      .drop(offset)
      .take(limit)
      .result
      .map { rows =>
        rows
          .groupBy { case (id, name, pos, colId, updatedAt, _) =>
            (id, name, pos, colId, updatedAt)
          }
          .map { case ((id, name, pos, colId, updatedAt), group) =>
            val memberIds = group.flatMap(_._6).distinct
            TaskSummaryResponse(id, name, pos, colId, memberIds, updatedAt)
          }
          .toSeq
          .sortBy(_.position)
      }
  }

  implicit val getTaskResult
  : GetResult[(Int, String, Int, Int, Instant, Int, Int)] = GetResult { r =>
    (
      r.<<[Int],
      r.<<[String],
      r.<<[Int],
      r.<<[Int],
      r.<<[java.sql.Timestamp].toInstant,
      r.<<[Int],
      r.<<[Int]
    )
  }

  def findLimitedActiveTasksByProject(projectId: Int): DBIO[Seq[(Int, String, Int, Int, Instant, Int, Int)]] = {
    val activeColumnStatus = ColumnStatus.active.toString
    val activeTaskStatus = TaskStatus.active.toString

    sql"""
    SELECT id, name, position, column_id, updated_at, rn, total_tasks_in_column
    FROM (
      SELECT
        t.id,
        t.name,
        t.position,
        t.column_id,
        t.updated_at,
        ROW_NUMBER() OVER (PARTITION BY t.column_id ORDER BY t.position) as rn,
        COUNT(*) OVER (PARTITION BY t.column_id) AS total_tasks_in_column
      FROM tasks t
      INNER JOIN columns c ON c.id = t.column_id
      WHERE c.project_id = $projectId
        AND c.status = $activeColumnStatus::column_status
        AND t.status = $activeTaskStatus::task_status
    ) ranked
    WHERE rn <= 20
    ORDER BY column_id, position
  """.as[(Int, String, Int, Int, Instant, Int, Int)]
  }

  def findUserTaskByTaskIds(taskIds: Set[Int]): DBIO[Seq[UserTask]] =
    if (taskIds.nonEmpty) {
      userTasks.filter(_.taskId inSet taskIds).result
    } else {
      DBIO.successful(Seq.empty)
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

  def updatePosition(taskId: Int, position: Int, columnId: Int): DBIO[Int] = {
    tasks
      .filter(_.id === taskId)
      .map(t => (t.position, t.columnId))
      .update((position, columnId))
  }

}
