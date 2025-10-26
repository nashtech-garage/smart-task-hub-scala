package cache

import dto.response.task.TaskSummaryResponse
import models.entities.Task
import play.api.Logger
import play.api.cache.AsyncCacheApi

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaskCacheManager @Inject() (cache: AsyncCacheApi)(implicit ec: ExecutionContext) {

  private val logger = Logger("application")

  private val indexPrefix = "task_index_column_"
  private val taskPrefix = "task_data_"

  def addTasksToColumn(columnId: Int,
                       tasks: Seq[TaskSummaryResponse]): Future[Unit] = {
    logger.info("Adding tasks to cache for column " + columnId)
    getSortedIndex(columnId).flatMap { oldIndex =>
      val newItems = tasks.map(t => (t.id, t.position))
      val updated = (oldIndex ++ newItems).sortBy(_._2)

      tasks.foreach(t => cache.set(taskKey(t.id), t))

      cache.set(indexKey(columnId), updated).map(_ => ())
    }
  }

  def addSingleTask(columnId: Int, task: TaskSummaryResponse): Future[Unit] = {
    getSortedIndex(columnId).flatMap { oldIndex =>
      val updated = ((task.id, task.position) :: oldIndex)
        .sortBy(_._2)

      cache.set(taskKey(task.id), task)
      cache.set(indexKey(columnId), updated).map(_ => ())
    }
  }

  def removeTask(columnId: Int, taskId: Int): Future[Unit] = {
    getSortedIndex(columnId).flatMap { oldIndex =>
      val updated = oldIndex.filterNot(_._1 == taskId)
      cache.remove(taskKey(taskId))
      cache.set(indexKey(columnId), updated).map(_ => ())
    }
  }

  def updateTaskFields(updatedTask: Task): Future[Unit] = {
    val taskId = updatedTask.id.get
    val columnId = updatedTask.columnId

    cache.get[TaskSummaryResponse](taskKey(taskId)).flatMap {
      case Some(cached) =>
        val merged = cached.copy(
          name = updatedTask.name,
          position = updatedTask.position,
          updatedAt = updatedTask.updatedAt
        )

        val updateDataF = cache.set(taskKey(taskId), merged)

        val updateIndexF = getSortedIndex(columnId).flatMap { indexList =>
          val newIndexList = indexList
            .map {
              case (id, _) if id == taskId =>
                (taskId, updatedTask.position)
              case other => other
            }
            .sortBy(_._2)

          cache.set(indexKey(columnId), newIndexList)
        }

        for {
          _ <- updateDataF
          _ <- updateIndexF
        } yield ()

      case None =>
        Future.unit
    }
  }

  private def taskKey(taskId: Int) = s"$taskPrefix$taskId"

  private def getSortedIndex(columnId: Int): Future[List[(Int, Int)]] =
    cache
      .get[List[(Int, Int)]](indexKey(columnId))
      .map(_.getOrElse(Nil).sortBy(_._2))

  private def indexKey(columnId: Int) = s"$indexPrefix$columnId"

  def getTasks(columnId: Int, offset: Int, limit: Int)(
    implicit ec: ExecutionContext
  ): Future[Option[Seq[TaskSummaryResponse]]] = {

    // cache.get returns Future[Option[T]] for AsyncCacheApi
    cache.get[List[(Int, Int)]](indexKey(columnId)).flatMap {
      case None =>
        // no index cached for this column
        Future.successful(None)

      case Some(indexList) =>
        // sortBy on the actual List, not on Option
        val sorted: List[(Int, Int)] = indexList.sortBy(_._2)
        val slice: List[Int] = sorted.slice(offset, offset + limit).map(_._1)

        // fetch details for each id from cache (each cache.get returns Future[Option[TaskSummaryResponse]])
        val detailFutures: Seq[Future[Option[TaskSummaryResponse]]] =
          slice.map(id => cache.get[TaskSummaryResponse](taskKey(id)))

        // sequence -> Future[Seq[Option[TaskSummaryResponse]]], then flatten present ones
        Future.sequence(detailFutures).map { seqOptDetails =>
          val tasks: Seq[TaskSummaryResponse] = seqOptDetails.flatten
          if (tasks.nonEmpty) Some(tasks) else None
        }
    }
  }
  def updateTaskPosition(newColumnId: Int, taskId: Int, newPosition: Int): Future[Unit] = {
    cache.get[TaskSummaryResponse](taskKey(taskId)).flatMap {
      case Some(cachedTask) =>
        val updatedTask = cachedTask.copy(position = newPosition)

        val updateDataF = cache.set(taskKey(taskId), updatedTask)

        val updateIndexF = getSortedIndex(newColumnId).flatMap { indexList =>
          val updatedIndexList = indexList.map {
            case (id, _) if id == taskId =>
              (taskId, newPosition)
            case other => other
          }.sortBy(_._2)

          cache.set(indexKey(newColumnId), updatedIndexList)
        }

        for {
          _ <- updateDataF
          _ <- updateIndexF
        } yield ()

      case None =>
        logger.warn(s"Task $taskId not found in cache → skip update position")
        Future.unit
    }
  }

}
