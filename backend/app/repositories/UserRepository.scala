package repositories

import models.entities.User
import models.tables.TableRegistry
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
}