package repositories

import javax.inject.{Inject, Singleton}
import models.entities.UserProfile
import models.tables.UserProfileTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.jdbc.PostgresProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserProfileRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfig[PostgresProfile] with UserProfileTable {

  import profile.api._
  
  def create(userProfile: UserProfile): Future[UserProfile] = {
    val query = userProfiles returning userProfiles.map(_.id) into ((profile, id) => profile.copy(id = id))
    db.run(query += userProfile)
  }

  def update(userProfile: UserProfile): Future[Int] = {
    val query = userProfiles.filter(_.id === userProfile.id).update(userProfile)
    db.run(query)
  }

  def findByUserId(userId: Int): Future[Option[UserProfile]] = {
    val query = userProfiles.filter(_.userId === userId)
    db.run(query.result.headOption)
  }

  def findById(id: Int): Future[Option[UserProfile]] = {
    val query = userProfiles.filter(_.id === id)
    db.run(query.result.headOption)
  }

  def delete(id: Int): Future[Int] = {
    val query = userProfiles.filter(_.id === id).delete
    db.run(query)
  }
}