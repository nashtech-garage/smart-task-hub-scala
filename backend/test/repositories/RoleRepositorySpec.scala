package repositories

import com.typesafe.config.ConfigFactory
import models.entities.Role
import models.tables.{RoleTable, TableRegistry}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class RoleRepositorySpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach  {

  // configuration for the test application
  override def fakeApplication(): Application = {
    val config = ConfigFactory.load("application.test.conf")
    new GuiceApplicationBuilder()
      .configure(Configuration(config))
      .build()
  }

  lazy val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  val repo = new RoleRepository(dbConfigProvider)(ExecutionContext.global)

  "RoleRepository" should {

    "find role by name" in {
      val result = Await.result(repo.findByRoleName("admin"), 3.seconds)
      result mustBe defined
      result.get.id mustBe Some(2)
    }

    "find role by id" in {
      val result = Await.result(repo.findByRoleId(1), 3.seconds)
      result mustBe defined
      result.get.name mustBe "user"
    }

    "return None for non-existent role name" in {
      val result = Await.result(repo.findByRoleName("not_exist"), 3.seconds)
      result mustBe empty
    }
  }
}
