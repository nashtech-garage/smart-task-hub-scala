package controllers

import dto.request.task.{
  AssignMemberRequest,
  CreateTaskRequest,
  UpdateTaskRequest
}
import dto.response.task.{TaskSearchResponse, TaskSummaryResponse}
import exception.AppException
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration}
import services._

class TaskControllerSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with Injecting
    with ScalaFutures
    with BeforeAndAfterAll {

  override implicit def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "config.resource" -> "application.test.conf",
        "slick.dbs.default.db.url" -> s"jdbc:h2:mem:tasktest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
      )
      .build()
  }

  // config & token
  lazy val config: Configuration = app.configuration
  lazy val defaultAdminEmail: String =
    config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
  lazy val defaultAdminName: String =
    config.getOptional[String]("admin.name").getOrElse("Administrator")
  lazy val cookieName: String =
    config.getOptional[String]("cookie.name").getOrElse("auth_token")

  def fakeToken: String = {
    val jwtService = inject[JwtService]
    jwtService
      .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
      .getOrElse(throw new RuntimeException("JWT token not generated"))
  }

  override def beforeAll(): Unit = {
    val workspaceService = inject[WorkspaceService]
    val projectService = inject[ProjectService]

    await(
      workspaceService.createWorkspace(
        dto.request.workspace.CreateWorkspaceRequest("Workspace test"),
        1
      )
    )

    await(
      // create project with default columns
      projectService.createProject(
        dto.request.project.CreateProjectRequest("Project test"),
        1,
        1
      )
    )
  }

  "TaskController" should {

    "create task successfully" in {
      val body = Json.toJson(CreateTaskRequest("Task 1", 1))
      val request = FakeRequest(POST, "/api/columns/1/tasks")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String] must include(
        "Task created successfully"
      )
    }

    "fail when creating task with unexisting column id" in {
      val body = Json.toJson(CreateTaskRequest("Task 1", 1))
      val request = FakeRequest(POST, "/api/columns/0/tasks")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val ex = intercept[AppException] {
        await(route(app, request).get)
      }
      ex.statusCode mustBe NOT_FOUND
      ex.message must include(
        "Column with ID 0 does not exist or is not active"
      )
    }

    "assign member into a task successfully" in {
      val body = Json.toJson(AssignMemberRequest(1))
      val request = FakeRequest(POST, "/api/projects/1/tasks/1/members")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] must include(
        "Member assigned to task successfully"
      )
    }

    "fail when assign member with unexist task id" in {
      val body = Json.toJson(AssignMemberRequest(1))
      val request = FakeRequest(POST, "/api/projects/1/tasks/0/members")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val ex = intercept[AppException] {
        await(route(app, request).get)
      }

      ex.statusCode mustBe NOT_FOUND
      ex.message must include("Task with ID 0 does not exist")
    }

    "unassign member from a task successfully" in {
      val request = FakeRequest(DELETE, "/api/projects/1/tasks/1/members/1")
        .withCookies(Cookie(cookieName, fakeToken))

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] must include(
        "Member unassigned from task successfully"
      )
    }

    "fail when unassigning member from a task with user not assign to a task" in {
      val request = FakeRequest(DELETE, "/api/projects/1/tasks/1/members/0")
        .withCookies(Cookie(cookieName, fakeToken))

      val result = route(app, request).get

      val ex = intercept[AppException] {
        await(result)
      }
      ex.statusCode mustBe BAD_REQUEST
      ex.message must include(
        "User with ID 0 is not in the project or not assigned to the task"
      )
    }

    "fail when unassigning member from a task with user not in project" in {
      val request = FakeRequest(DELETE, "/api/projects/2/tasks/1/members/0")
        .withCookies(Cookie(cookieName, fakeToken))

      val result = route(app, request).get

      val ex = intercept[AppException] {
        await(result)
      }
      ex.statusCode mustBe FORBIDDEN
      ex.message must include(
        "You do not have permission to unassign members from this task"
      )
    }

    "fail when creating task with duplicate position in same column" in {
      val body = Json.toJson(CreateTaskRequest("Task 1", 1))
      val request = FakeRequest(POST, "/api/columns/1/tasks")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val resultFut = route(app, request).get
      val ex = intercept[AppException] {
        await(resultFut)
      }

      ex.statusCode mustBe CONFLICT
      ex.message must include("Task position already exists in the column")
    }

    "update task successfully" in {
      val body =
        Json.toJson(
          UpdateTaskRequest("Updated Task", None, None, None, None, None)
        )
      val request = FakeRequest(PATCH, "/api/tasks/1")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)
        .withHeaders(CONTENT_TYPE -> "application/json")

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task updated successfully"
    }

    "fail when updating task with unexist task id" in {
      val body =
        Json.toJson(
          UpdateTaskRequest("Updated Task", None, None, None, None, None)
        )
      val request = FakeRequest(PATCH, "/api/tasks/0")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)
        .withHeaders(CONTENT_TYPE -> "application/json")

      val ex = intercept[AppException] {
        await(route(app, request).get)
      }

      ex.statusCode mustBe NOT_FOUND
      ex.message must include("Task with ID 0 does not exist")
    }

    "fail to update non-existent task" in {
      val nonExistentTaskId = 9999
      val body =
        Json.toJson(
          UpdateTaskRequest("Some Task", None, None, None, None, None)
        )
      val request = FakeRequest(PATCH, s"/api/tasks/$nonExistentTaskId")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val resultFut = route(app, request).get
      val ex = intercept[AppException] {
        await(resultFut)
      }

      ex.statusCode mustBe NOT_FOUND
      ex.message must include(s"Task with ID $nonExistentTaskId does not exist")
    }

    "get task by id successfully" in {
      val request = FakeRequest(GET, "/api/tasks/1")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task retrieved successfully"
    }

    "archive task successfully" in {
      val request = FakeRequest(PATCH, "/api/tasks/1/archive")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task archived successfully"
    }

    "restore task successfully" in {
      val request = FakeRequest(PATCH, "/api/tasks/1/restore")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task restored successfully"
    }

    "delete task successfully" in {
      val archiveRequest = FakeRequest(PATCH, "/api/tasks/1/archive")
        .withCookies(Cookie(cookieName, fakeToken))
      await(route(app, archiveRequest).get)

      val request = FakeRequest(DELETE, "/api/tasks/1")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Task deleted successfully"
    }

    "get archived tasks successfully" in {
      val columnService = inject[ColumnService]
      val taskService = inject[TaskService]

      // Create and archive a task in the new column
      val taskId = await(
        taskService.createNewTask(CreateTaskRequest("Task to Archive", 1), 1, 1)
      )
      await(taskService.archiveTask(taskId, 1))

      val request = FakeRequest(GET, "/api/projects/1/columns/tasks/archived")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Archived tasks retrieved successfully"
      val tasks = (contentAsJson(result) \ "data").as[Seq[TaskSummaryResponse]]
      tasks.exists(task => task.id == taskId && task.name == "Task to Archive") mustBe true
    }

    "fail when get archived tasks with unexist project id" in {
      val request = FakeRequest(GET, "/api/projects/0/columns/tasks/archived")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      val ex = intercept[AppException] {
        await(result)
      }
      ex.statusCode mustBe NOT_FOUND
      ex.message must include("Project not found")
    }

    "get active tasks successfully" in {
      val taskService = inject[TaskService]

      // Create a new task in the existing column
      val taskId = await(
        taskService.createNewTask(CreateTaskRequest("Active Task", 1), 1, 1)
      )

      val request = FakeRequest(GET, "/api/projects/1/columns/tasks/active")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Active tasks retrieved successfully"
      val tasks = (contentAsJson(result) \ "data").as[Seq[TaskSummaryResponse]]
      tasks.exists(task => task.id == taskId && task.name == "Active Task") mustBe true
    }

    "fail get active tasks with unexist project id" in {
      val request = FakeRequest(GET, "/api/projects/0/columns/tasks/active")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      val ex = intercept[AppException] {
        await(result)
      }
      ex.statusCode mustBe NOT_FOUND
      ex.message must include("Project not found")
    }

    "search tasks successfully" in {
      val taskService = inject[TaskService]

      // Create a new task in the existing column
      val taskId = await(
        taskService.createNewTask(CreateTaskRequest("search Task", 1502), 1, 1)
      )

      val request = FakeRequest(GET, "/api/tasks?page=1&size=10&keyword=search")
        .withCookies(Cookie(cookieName, fakeToken))
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message")
        .as[String] mustBe "Tasks retrieved successfully"
      val tasks = (contentAsJson(result) \ "data").as[Seq[TaskSearchResponse]]
      tasks.length must be > 0
    }
  }
}
