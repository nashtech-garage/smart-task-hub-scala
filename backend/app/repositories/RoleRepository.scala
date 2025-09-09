package repositories

import models.entities.Role
import models.tables.TableRegistry
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
 * Repository for performing operations on Role entities.
 *
 * @param dbConfigProvider Provides the database configuration.
 * @param ec The execution context for asynchronous operations.
 */
@Singleton
class RoleRepository @Inject()(
                                protected val dbConfigProvider: DatabaseConfigProvider
                              )(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Reference to the roles table from the table registry
  private val roles = TableRegistry.roles

  /**
   * Finds a role by its name.
   *
   * @param roleName The name of the role to search for.
   * @return A Future containing an Option with the Role if found, or None.
   */
  def findByRoleName(roleName: String): Future[Option[Role]] = {
    db.run(roles.filter(_.name === roleName).result.headOption)
  }

  def findByRoleId(roleId: Int): Future[Option[Role]] = {
    db.run(roles.filter(_.id === roleId).result.headOption)
  }

  def create(role: Role): Future[Role] = {
    val insertQuery = roles returning roles.map(_.id) into ((role, id) => role.copy(id = Some(id)))
    db.run(insertQuery += role)
  }
}