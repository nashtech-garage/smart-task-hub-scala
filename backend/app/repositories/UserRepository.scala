package repositories

import dto.response.user.UserPublicDTO
import models.entities.{User, UserProject, UserTask}
import models.tables.TableRegistry
import models.tables.TableRegistry.{userProjects, userTasks}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Repository for performing CRUD operations on User entities.
 *
 * @param dbConfigProvider Provides the database configuration.
 * @param ec The execution context for asynchronous operations.
 */
@Singleton
class UserRepository @Inject()(
                                protected val dbConfigProvider: DatabaseConfigProvider
                              )(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val users = TableRegistry.users

  /**
   * Creates a new user in the database.
   *
   * @param user The user entity to create.
   * @return A Future containing the created user with its generated ID.
   */
  def create(user: User): Future[User] = {
    val insertQuery = users returning users.map(_.id) into ((user, id) => user.copy(id = Some(id)))
    db.run(insertQuery += user)
  }

  /**
   * Finds a user by email address.
   *
   * @param email The email address to search for.
   * @return A Future containing an Option with the user if found, or None.
   */
  def findByEmail(email: String): Future[Option[User]] = {
    db.run(users.filter(_.email === email).result.headOption)
  }

  /**
   * Finds a user by ID.
   *
   * @param id The user ID.
   * @return A Future containing an Option with the user if found, or None.
   */
  def findById(id: Int): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  /**
   * Updates an existing user.
   *
   * @param user The user entity with updated data.
   * @return A Future containing the number of affected rows.
   */
  def update(user: User): Future[Int] = {
    db.run(users.filter(_.id === user.id).update(user))
  }

  def createBatch(userList: Seq[User]): Future[Seq[User]] = {
    val insertQuery = users returning users.map(_.id) into ((user, id) => user.copy(id = Some(id)))
    val action = insertQuery ++= userList
    db.run(action)
  }

  def findUsersInProjectByProjectId(projectId: Int): DBIO[Seq[UserProject]] =
    userProjects.filter(_.projectId === projectId).result

  def findUserTasksByTaskIds(taskIds: Seq[Int]): DBIO[Seq[UserTask]] =
    userTasks.filter(_.taskId.inSet(taskIds)).result

  def findPublicByIds(userIds: Seq[Int]): DBIO[Seq[UserPublicDTO]] =
    users
      .filter(_.id.inSet(userIds))
      .map(u => (u.id, u.name))
      .result
      .map(_.map { case (id, name) => UserPublicDTO(id, name) })
}