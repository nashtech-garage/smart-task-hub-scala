package repositories

import com.typesafe.config.ConfigFactory
import models.entities.{Project, User, UserProject, Workspace}
import models.Enums.{ProjectStatus, ProjectVisibility, UserProjectRole, WorkspaceStatus}
import models.tables.TableRegistry
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import slick.jdbc.JdbcProfile

import java.time.{Instant, LocalDateTime}
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class ProjectRepositorySpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach {

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

  val projectRepository = new ProjectRepository(dbConfigProvider)(ExecutionContext.global)

  override def beforeEach(): Unit = {
    val now = Instant.now()
    val localNow = LocalDateTime.now()

    // Disable foreign key checks for H2
    val disableFK = sqlu"SET REFERENTIAL_INTEGRITY FALSE"
    val enableFK = sqlu"SET REFERENTIAL_INTEGRITY TRUE"

    val setup = DBIO.seq(
      disableFK,

      // Clear tables in H2 (using TRUNCATE is more reliable)
      sqlu"TRUNCATE TABLE user_projects RESTART IDENTITY",
      sqlu"TRUNCATE TABLE projects RESTART IDENTITY",
      sqlu"TRUNCATE TABLE workspaces RESTART IDENTITY",
      sqlu"TRUNCATE TABLE users RESTART IDENTITY",
      sqlu"TRUNCATE TABLE roles RESTART IDENTITY",

      enableFK,

      // Insert test data using raw SQL for H2 compatibility
      sqlu"INSERT INTO roles (name) VALUES ('user')",

      sqlu"INSERT INTO users (id, name, email, password, role_id, created_at, updated_at) VALUES (2, 'Test User 1', 'user1@test.com', 'hashed_password', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO users (id, name, email, password, role_id, created_at, updated_at) VALUES (3, 'Test User 2', 'user2@test.com', 'hashed_password', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO workspaces (id, name, description, status, created_by, created_at, updated_at, is_deleted) VALUES (1, 'Test Workspace 1', 'Test workspace description', 'active', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)",

      sqlu"INSERT INTO workspaces (id, name, description, status, created_by, created_at, updated_at, is_deleted) VALUES (2, 'Test Workspace 2', 'Another test workspace', 'active', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)",

      sqlu"INSERT INTO projects (id, name, workspace_id, status, visibility, created_by, created_at, updated_at) VALUES (1, 'Active Project 1', 1, 'active', 'workspace', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO projects (id, name, workspace_id, status, visibility, created_by, created_at, updated_at) VALUES (2, 'Active Project 2', 1, 'active', 'private', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO projects (id, name, workspace_id, status, visibility, created_by, created_at, updated_at) VALUES (3, 'Completed Project', 1, 'completed', 'workspace', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO projects (id, name, workspace_id, status, visibility, created_by, created_at, updated_at) VALUES (4, 'Deleted Project', 1, 'deleted', 'workspace', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO user_projects (id, user_id, project_id, role, invited_by, joined_at) VALUES (1, 2, 1, 'owner', NULL, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO user_projects (id, user_id, project_id, role, invited_by, joined_at) VALUES (2, 3, 2, 'owner', NULL, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO user_projects (id, user_id, project_id, role, invited_by, joined_at) VALUES (3, 2, 3, 'owner', NULL, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO user_projects (id, user_id, project_id, role, invited_by, joined_at) VALUES (4, 2, 4, 'owner', NULL, CURRENT_TIMESTAMP)",

      sqlu"INSERT INTO user_projects (id, user_id, project_id, role, invited_by, joined_at) VALUES (5, 3, 1, 'member', 2, CURRENT_TIMESTAMP)"
    )

    Await.result(db.run(setup), 15.seconds)
  }

  "ProjectRepository" should {

//    "create project with owner successfully" in {
//      val now = Instant.now()
//      val newProject = Project(
////        id = Some(5),
//        name = "New Test Project",
//        workspaceId = 1,
//        status = ProjectStatus.active,
//        visibility = ProjectVisibility.Workspace,
//        createdBy = Some(1),
//        createdAt = now,
//        updatedAt = now
//      )
//
//      val result = Await.result(db.run(projectRepository.createProjectWithOwner(newProject, 2)), 5.seconds)
//
//      println(s"Created project ID: $result")
//
//      result must be > 0
//
//      // Verify project was created
//      val projects = TableRegistry.projects
//      val createdProject = Await.result(db.run(projects.filter(_.id === result).result.headOption), 3.seconds)
//      createdProject mustBe defined
//      createdProject.get.name mustBe "New Test Project"
//
//      // Verify owner relationship was created
//      val ownerRelation = Await.result(db.run(
//        sql"SELECT id, user_id, project_id, role FROM user_projects WHERE project_id = $result AND user_id = 2".as[(Int, Int, Int, String)]
//      ), 3.seconds)
//
//      ownerRelation must not be empty
//      val (_, userId, projectId, role) = ownerRelation.head
//      userId mustBe 2
//      projectId mustBe result
//      role mustBe "owner"
//    }

    "find non-deleted projects by workspace" in {
      val result = Await.result(db.run(projectRepository.findNonDeletedByWorkspace(1)), 5.seconds)

      result must have size 2
      val projectNames = result.map(_.name).toSet
      projectNames must contain("Active Project 1")
      projectNames must contain("Active Project 2")
      projectNames must not contain("Completed Project")
      projectNames must not contain("Deleted Project")
    }

    "find project status if user is owner" in {
      val result = Await.result(db.run(projectRepository.findStatusIfOwner(1, 2)), 5.seconds)

      result mustBe defined
      result.get mustBe ProjectStatus.active
    }

    "return None when user is not owner" in {
      val result = Await.result(db.run(projectRepository.findStatusIfOwner(2, 2)), 5.seconds)

      // User 1 is not owner of project 2 (user 2 is the owner)
      result mustBe empty
    }

    "update project status successfully" in {
      val result = Await.result(db.run(projectRepository.updateStatus(1, ProjectStatus.completed)), 5.seconds)

      result mustBe 1

      // Verify status was updated
      val projects = TableRegistry.projects
      val updatedProject = Await.result(db.run(projects.filter(_.id === 1).result.head), 3.seconds)
      updatedProject.status mustBe ProjectStatus.completed
    }

    "find completed projects by user id" in {
      val result = Await.result(db.run(projectRepository.findCompletedProjectsByUserId(2)), 5.seconds)

      result must have size 1
      val completedProject = result.head
      completedProject.id mustBe 3
      completedProject.name mustBe "Completed Project"
      completedProject.workspaceName mustBe "Test Workspace 1"
    }

    "return empty list when user has no completed projects" in {
      // User 2 has no completed projects as owner (only project 2 which is active)
      val result = Await.result(db.run(projectRepository.findCompletedProjectsByUserId(3)), 5.seconds)

      result mustBe empty
    }

  }
}