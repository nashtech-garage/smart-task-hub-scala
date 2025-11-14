package services

import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory
import dto.request.workspace.{CreateWorkspaceRequest, InviteUserIntoWorkspaceRequest, UpdateWorkspaceRequest}
import exception.AppException
import models.tables.TableRegistry
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class WorkspaceServiceISpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach
    with ScalaFutures {

  override def fakeApplication(): Application = {
    val config = ConfigFactory.load("application.test.conf")
    val mockEmailService = mock(classOf[EmailService])

    new GuiceApplicationBuilder()
      .configure(Configuration(config))
      .overrides(new AbstractModule {
        override def configure(): Unit = {
          bind(classOf[EmailService]).toInstance(mockEmailService)
        }
      })
      .build()
  }

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val patience: PatienceConfig =
    PatienceConfig(timeout = Span(15, Seconds), interval = Span(100, org.scalatest.time.Millis))

  lazy val dbConfigProvider: DatabaseConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  lazy val dbConfig = dbConfigProvider.get[JdbcProfile]
  lazy val db: slick.jdbc.JdbcBackend#Database = dbConfig.db
  import dbConfig.profile.api._

  lazy val workspaceService: WorkspaceService = app.injector.instanceOf[WorkspaceService]

  override def beforeEach(): Unit = {
    val disableFK = sqlu"SET REFERENTIAL_INTEGRITY FALSE"
    val enableFK = sqlu"SET REFERENTIAL_INTEGRITY TRUE"

    val setup = DBIO.seq(
      disableFK,
      sqlu"TRUNCATE TABLE user_workspaces RESTART IDENTITY",
      sqlu"TRUNCATE TABLE workspaces RESTART IDENTITY",
      sqlu"TRUNCATE TABLE users RESTART IDENTITY",
      sqlu"TRUNCATE TABLE roles RESTART IDENTITY",
      enableFK,
      sqlu"INSERT INTO roles (name) VALUES ('user')",
      sqlu"INSERT INTO users (id, name, email, password, role_id, created_at, updated_at) VALUES (1, 'Test User 1', 'user1@test.com', 'hashed_password', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
      sqlu"INSERT INTO users (id, name, email, password, role_id, created_at, updated_at) VALUES (2, 'Test User 2', 'user2@test.com', 'hashed_password', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
      sqlu"INSERT INTO users (id, name, email, password, role_id, created_at, updated_at) VALUES (3, 'Test User 3', 'user3@test.com', 'hashed_password', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
    )

    db.run(setup).futureValue
  }

  "WorkspaceService#createWorkspace" should {
    "successfully create a workspace" in {
      val request = CreateWorkspaceRequest("Test Workspace", Some("Test Description"))
      val userId = 1

      val createdId: Int = workspaceService.createWorkspace(request, userId).futureValue

      createdId must be > 0

      val workspaces = db.run(
        TableRegistry.workspaces
          .filter(_.name === "Test Workspace")
          .result
      ).futureValue

      workspaces must have length 1
      val workspace = workspaces.head
      workspace.name mustBe "Test Workspace"
      workspace.description mustBe Some("Test Description")
      workspace.createdBy mustBe Some(1)
    }

    "reject duplicate workspace names for same user" in {
      val request = CreateWorkspaceRequest("Duplicate WS", Some("Desc"))
      val userId = 1

      workspaceService.createWorkspace(request, userId).futureValue

      // Second creation should fail with AppException wrapped in Future
      val result = workspaceService.createWorkspace(request, userId)

      result.failed.futureValue match {
        case ex: AppException =>
          ex.statusCode mustBe 409
          ex.message must include("already exists")
        case other =>
          fail(s"Expected AppException but got ${other.getClass.getSimpleName}")
      }
    }

    "allow same workspace name for different users" in {
      val request = CreateWorkspaceRequest("Shared Name", Some("Desc"))

      val id1 = workspaceService.createWorkspace(request, 1).futureValue
      val id2 = workspaceService.createWorkspace(request, 2).futureValue

      id1 must not equal id2
    }
  }

  "WorkspaceService#updateWorkspace" should {
    "successfully update workspace name and description" in {
      val createReq = CreateWorkspaceRequest("Original", Some("Original Desc"))
      val wsId = workspaceService.createWorkspace(createReq, 1).futureValue

      val updateReq = UpdateWorkspaceRequest("Updated", Some("Updated Desc"))
      val updated = workspaceService.updateWorkspace(wsId, updateReq, 1).futureValue

      updated mustBe 1

      val workspaces = db.run(
        TableRegistry.workspaces
          .filter(_.id === wsId)
          .result
      ).futureValue

      workspaces must have length 1
      val workspace = workspaces.head
      workspace.name mustBe "Updated"
      workspace.description mustBe Some("Updated Desc")
    }

    "fail when workspace not found" in {
      val updateReq = UpdateWorkspaceRequest("Name", Some("Desc"))

      val result = workspaceService.updateWorkspace(999, updateReq, 1)

      result.failed.futureValue match {
        case ex: AppException =>
          ex.statusCode mustBe 404
          ex.message must include("not found")
        case other =>
          fail(s"Expected AppException but got ${other.getClass.getSimpleName}")
      }
    }
  }

  "WorkspaceService#deleteWorkspace" should {
    "successfully delete workspace" in {
      val createReq = CreateWorkspaceRequest("To Delete", Some("Desc"))
      val wsId = workspaceService.createWorkspace(createReq, 1).futureValue

      val deleted = workspaceService.deleteWorkspace(wsId, 1).futureValue

      deleted mustBe true

      val workspaces = db.run(
        TableRegistry.workspaces
          .filter(_.id === wsId)
          .result
      ).futureValue

      workspaces mustBe empty
    }
  }

  "WorkspaceService#getAllWorkspaces" should {
    "return all workspaces for a user" in {
      val req1 = CreateWorkspaceRequest("Workspace 1", Some("Desc 1"))
      val req2 = CreateWorkspaceRequest("Workspace 2", Some("Desc 2"))

      workspaceService.createWorkspace(req1, 1).futureValue
      workspaceService.createWorkspace(req2, 1).futureValue

      val workspaces = workspaceService.getAllWorkspaces(1).futureValue

      workspaces must have length 2
      val names = workspaces.map(_.name).toSet
      names must contain("Workspace 1")
      names must contain("Workspace 2")
    }

    "return empty list when user has no workspaces" in {
      val workspaces = workspaceService.getAllWorkspaces(999).futureValue

      workspaces mustBe empty
    }
  }

  "WorkspaceService#inviteUserToWorkspace" should {
    "successfully invite user to workspace" in {
      val createReq = CreateWorkspaceRequest("Invite Test", Some("Desc"))
      val wsId = workspaceService.createWorkspace(createReq, 1).futureValue

      val inviteReq = InviteUserIntoWorkspaceRequest("user2@test.com")
      val insertedId = workspaceService.inviteUserToWorkspace(1, wsId, inviteReq).futureValue

      insertedId must be > 0

      val userWorkspaces = db.run(
        TableRegistry.userWorkspaces
          .filter(uw => uw.workspaceId === wsId && uw.userId === 2)
          .result
      ).futureValue

      userWorkspaces must have length 1
      val uw = userWorkspaces.head
      uw.role.toString mustBe "member"
      uw.status.toString mustBe "active"
    }

    "fail when inviter is not a member of workspace" in {
      val createReq = CreateWorkspaceRequest("Workspace", Some("Desc"))
      val wsId = workspaceService.createWorkspace(createReq, 1).futureValue

      val inviteReq = InviteUserIntoWorkspaceRequest("user2@test.com")

      val result = workspaceService.inviteUserToWorkspace(999, wsId, inviteReq)

      result.failed.futureValue match {
        case ex: AppException =>
          ex.statusCode mustBe 404
        case other =>
          fail(s"Expected AppException but got ${other.getClass.getSimpleName}")
      }
    }

    "fail when invitee user does not exist" in {
      val createReq = CreateWorkspaceRequest("Workspace", Some("Desc"))
      val wsId = workspaceService.createWorkspace(createReq, 1).futureValue

      val inviteReq = InviteUserIntoWorkspaceRequest("nonexistent@test.com")

      val result = workspaceService.inviteUserToWorkspace(1, wsId, inviteReq)

      result.failed.futureValue match {
        case ex: AppException =>
          ex.statusCode mustBe 404
          ex.message must include("not found")
        case other =>
          fail(s"Expected AppException but got ${other.getClass.getSimpleName}")
      }
    }

    "fail when user is already a member" in {
      val createReq = CreateWorkspaceRequest("Workspace", Some("Desc"))
      val wsId = workspaceService.createWorkspace(createReq, 1).futureValue

      val inviteReq = InviteUserIntoWorkspaceRequest("user2@test.com")
      workspaceService.inviteUserToWorkspace(1, wsId, inviteReq).futureValue

      val result = workspaceService.inviteUserToWorkspace(1, wsId, inviteReq)

      result.failed.futureValue match {
        case ex: AppException =>
          ex.statusCode mustBe 400
          ex.message must include("already a member")
        case other =>
          fail(s"Expected AppException but got ${other.getClass.getSimpleName}")
      }
    }
  }
}
