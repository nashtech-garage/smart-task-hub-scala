package repositories

import db.MyPostgresProfile.api.{columnStatusTypeMapper, projectStatusTypeMapper, taskStatusTypeMapper}
import dto.request.column.UpdateColumnRequest
import dto.response.column.{ColumnSummariesResponse, ColumnWithTasksResponse}
import dto.response.task.TaskSummaryResponse
import models.Enums.ColumnStatus.ColumnStatus
import models.Enums.{ColumnStatus, ProjectStatus, TaskStatus}
import models.entities.Column
import models.tables.TableRegistry.{columns, projects, tasks, userProjects}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ColumnRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def create(column: Column): DBIO[Int] = {
    columns returning columns.map(_.id) += column
  }

  def findByProjectId(projectId: Int): DBIO[Seq[Column]] =
    columns.filter( c => c.projectId === projectId && c.status =!= ColumnStatus.deleted).result

  def exitsByPosition(projectId: Int, position: Int): DBIO[Boolean] = {
    columns
      .filter(c => c.projectId === projectId && c.position === position && c.status === ColumnStatus.active)
      .exists
      .result
  }

  def findActiveColumnsWithTasks(
    projectId: Int
  ): DBIO[Seq[ColumnWithTasksResponse]] = {
    columns
      .filter(
        c => c.projectId === projectId && c.status === ColumnStatus.active
      )
      .sortBy(_.position)
      .result
      .flatMap { cols =>
        tasks
          .filter(t => t.columnId.inSet(cols.flatMap(_.id)) && t.status === TaskStatus.active)
          .map(t => (t.columnId, t.id, t.name, t.position))
          .sortBy(_._4.asc.nullsLast)
          .result
          .map { taskRows =>
            val grouped = taskRows.groupBy(_._1)
            cols.map { col =>
              ColumnWithTasksResponse(
                id = col.id.get,
                name = col.name,
                position = col.position,
                taskIds = grouped.getOrElse(col.id.get, Seq.empty).map(_._2),
                totalTasks = grouped.getOrElse(col.id.get, Seq.empty).size
              )
            }
          }
      }
  }

  def findActiveColumnsByProject(projectId: Int): DBIO[Seq[Column]] =
    columns
      .filter(c => c.projectId === projectId && c.status === ColumnStatus.active)
      .sortBy(_.position.asc)
      .result

  def update(column: UpdateColumnRequest, columnId: Int): DBIO[Int] = {
    columns
      .filter(_.id === columnId)
      .map(c => (c.name, c.updatedAt))
      .update(column.name, Instant.now())
  }

  def updateStatus(columnId: Int, status: ColumnStatus): DBIO[Int] = {
    columns.filter(_.id === columnId).map(_.status).update(status)
  }

  def updatePosition(columnId: Int, position: Int): DBIO[Int] = {
    columns.filter(_.id === columnId).map(_.position).update(position)
  }

  def findStatusAndProjectIdIfUserInProject(columnId: Int, userId: Int): DBIO[Option[(Int, ColumnStatus)]] = {
    val query = for {
      (((c, p), up)) <- columns
        .join(projects).on(_.projectId === _.id)
        .join(userProjects).on { case ((c, p), up) =>
          p.id === up.projectId && up.userId === userId.bind
        }
      if c.id === columnId.bind
    } yield (p.id, c.status)

    query.result.headOption
  }

  def findColumnIfUserInProject(columnId: Int, userId: Int): DBIO[Option[Column]] = {
    val query = for {
      ((c, p), up) <- columns
        .join(projects).on(_.projectId === _.id)
        .join(userProjects).on { case ((c, p), up) => p.id === up.projectId }
      if c.id === columnId &&
        c.status === ColumnStatus.active &&
        p.status === ProjectStatus.active &&
        up.userId === userId
    } yield c

    query.result.headOption
  }

  def findArchivedColumnsByProjectId(
    projectId: Int
  ): DBIO[Seq[ColumnSummariesResponse]] = {
    columns
      .filter(
        c => c.projectId === projectId && c.status === ColumnStatus.archived
      )
      .sortBy(_.updatedAt.desc)
      .map(c => (c.id, c.name, c.position))
      .result
      .map(_.map((ColumnSummariesResponse.apply _).tupled))
  }

  def insertColumnBatch(cols: Seq[Column]): Future[Seq[Column]] = {
    db.run((columns returning columns) ++= cols).map(_.toSeq)
  }

  def importColumnBatch(cols: Seq[Column]): DBIO[Seq[Int]] = {
    (columns returning columns.map(_.id)) ++= cols
  }

}
