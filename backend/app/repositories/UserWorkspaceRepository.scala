package repositories

import models.entities.UserWorkspace
import models.tables.TableRegistry
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserWorkspaceRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  private val userWorkspaces = TableRegistry.userWorkspaces

  /**
    * Creates a DBIO action to insert a new UserWorkspace record into the database.
    * The action will only be executed when passed to the `db.run` method.
    * @param userWorkspace The UserWorkspace entity to insert.
    * @return A DBIO action that returns the number of rows affected.
    */
  def insertAction(userWorkspace: UserWorkspace): DBIO[Int] = {
    userWorkspaces += userWorkspace
  }
}
