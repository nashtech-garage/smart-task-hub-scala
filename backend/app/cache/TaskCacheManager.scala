package cache

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}
import dto.response.task.TaskSummaryResponse
import play.api.Logger

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters.CollectionHasAsScala

case class TasksInColumnCacheKey(columnId: Int, limit: Int, offset: Int)

@Singleton
class TaskCacheManager @Inject() {

  private val taskCache: Cache[TasksInColumnCacheKey, Seq[TaskSummaryResponse]] =
    Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(10, TimeUnit.MINUTES)
      .build[TasksInColumnCacheKey, Seq[TaskSummaryResponse]]()

  def getInColumn(key: TasksInColumnCacheKey): Option[Seq[TaskSummaryResponse]] = {
    Option(taskCache.getIfPresent(key))
  }

  def put(key: TasksInColumnCacheKey, value: Seq[TaskSummaryResponse]): Unit = {
    taskCache.put(key, value)
  }

  def invalidateColumn(columnId: Int): Unit = {
    Logger("application").debug(s"Invalidated cache for column ${columnId} due to task update")

    Logger("application").debug(s"Cache size before invalidate: ${taskCache.asMap().size()}")

    val keysToInvalidate = taskCache.asMap().keySet().asScala
      .filter(_.columnId == columnId)

    keysToInvalidate.foreach(taskCache.invalidate)
  }
}