package init

import com.github.t3hnar.bcrypt._
import models.Enums._
import models.entities._
import play.api.{Configuration, Environment, Logger, Mode}
import repositories._

import java.time.{Instant, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class DataSeeder @Inject()(
                            env: Environment,
                            userRepository: UserRepository,
                            workspaceRepository: WorkspaceRepository,
                            projectRepository: ProjectRepository,
                            columnRepository: ColumnRepository,
                            taskRepository: TaskRepository,
                            config: Configuration
                          )(implicit ec: ExecutionContext) {

  private val log = Logger("application")
  private val rnd = new Random()

  private val NUM_USERS = config.get[Int]("seeder.numUsers")
  private val NUM_WORKSPACES = config.get[Int]("seeder.numWorkspaces")
  private val PROJECTS_PER_WORKSPACE_MIN = config.get[Int]("seeder.projectsPerWorkspaceMin")
  private val PROJECTS_PER_WORKSPACE_MAX = config.get[Int]("seeder.projectsPerWorkspaceMax")
  private val DEFAULT_COLUMNS = Seq("To do", "In Progress", "Review", "Bug", "Done")
  private val TOTAL_TASKS_PER_PROJECT = config.get[Int]("seeder.totalTasksPerProject")
  private val TASK_BATCH = config.get[Int]("seeder.taskBatch")
  private val PCT_ASSIGNED_TO_TESTER = 0.20 // 20%

  def seedAll(): Future[Unit] = {
    log.info("Environment mode: " + env.mode)
    if (env.mode != Mode.Dev) {
      log.info("Skipping seeding: not running in dev environment")
      Future.successful(())
    } else {
      userRepository.findByEmail("tester@test.com").flatMap {
        case Some(_) =>
          log.info("Skipping seeding: tester@test.com already exists")
          Future.successful(())
        case None =>
          log.info("Start seeding data...")
          for {
            users <- seedUsers()
            workspaceIds <- seedWorkspaces(users)
            _ <- seedUserWorkspaces(users, workspaceIds)
            projects <- seedProjects(workspaceIds, users)
            _ <- seedUserProjects(users, projects)
            columns <- seedColumns(projects)
            _ <- seedTasksAndAssignments(columns, users)
          } yield {
            log.info("Seeding finished.")
          }
      }
    }
  }

  private def seedUsers(): Future[Seq[User]] = {
    val nowLd = LocalDateTime.now()
    val adminEmail = "admin@test.com"
    val testerEmail = "tester@test.com"

    val adminPwHashed = "123".bcryptSafeBounded.getOrElse("admin123")
    val testerPwHashed = "123".bcryptSafeBounded.getOrElse("tester123")
    val baseUsers = Seq(
      User(None, "Admin", adminEmail, adminPwHashed, None, Some(1), nowLd, nowLd),
      User(None, "Tester", testerEmail, testerPwHashed, None, Some(1), nowLd, nowLd)
    )

    val others = (1 to (NUM_USERS - baseUsers.length)).map { i =>
      val email = s"user$i@test.com"
      User(None, s"User $i", email, "pw".bcryptSafeBounded.getOrElse("pw"), None, Some(1), nowLd, nowLd)
    }

    val all = baseUsers ++ others
    userRepository.createBatch(all)
  }

  private def seedWorkspaces(users: Seq[User]): Future[Seq[Int]] = {
    val now = Instant.now()
    val adminOpt = users.find(_.email == "admin@test.com")
    val adminIdOpt = adminOpt.flatMap(_.id)

    val wsList = (1 to NUM_WORKSPACES).map { i =>
      Workspace(
        id = None,
        name = s"Workspace $i",
        description = Some(s"Auto-generated workspace $i"),
        status = models.Enums.WorkspaceStatus.active,
        createdBy = adminIdOpt,
        createdAt = Some(now),
        updatedAt = Some(now),
        updatedBy = adminIdOpt
      )
    }

    workspaceRepository.insertWorkspaceBatch(wsList)
  }

  private def seedUserWorkspaces(users: Seq[User], workspaceIds: Seq[Int]): Future[Seq[Int]] = {
    val now = Instant.now()
    val adminId = users.find(_.email == "admin@test.com").flatMap(_.id).get
    val testerId = users.find(_.email == "tester@test.com").flatMap(_.id).get

    val entries = workspaceIds.flatMap { wsId =>
      // always add admin + tester
      val base = Seq(
        UserWorkspace(None, adminId, wsId, UserWorkspaceRole.admin, UserWorkspaceStatus.active, None, now),
        UserWorkspace(None, testerId, wsId, UserWorkspaceRole.member, UserWorkspaceStatus.active, Some(adminId), now)
      )

      // add 3 random other users (if available)
      val otherUsers = rnd.shuffle(users.filter(u => u.email != "admin@test.com" && u.email != "tester@test.com")).take(3).flatMap(_.id)
      val others = otherUsers.map(uid =>
        UserWorkspace(None, uid, wsId, UserWorkspaceRole.member, UserWorkspaceStatus.active, Some(adminId), now))
      base ++ others
    }

    workspaceRepository.insertUserBatchIntoWorkspace(entries)
  }

  private def seedProjects(workspaceIds: Seq[Int], users: Seq[User]): Future[Seq[Project]] = {
    val now = Instant.now()
    val adminIdOpt = users.find(_.email == "admin@test.com").flatMap(_.id)

    val projects = workspaceIds.flatMap { wsId =>
      val count = rnd.between(PROJECTS_PER_WORKSPACE_MIN, PROJECTS_PER_WORKSPACE_MAX + 1)
      (1 to count).map { i =>
        Project(
          id = None,
          name = s"Project-$wsId-$i",
          workspaceId = wsId,
          status = models.Enums.ProjectStatus.active,
          visibility = models.Enums.ProjectVisibility.Private,
          createdBy = adminIdOpt,
          updatedBy = adminIdOpt,
          createdAt = now,
          updatedAt = now
        )
      }
    }

    projectRepository.insertProjectBatch(projects)
  }

  private def seedUserProjects(users: Seq[User], projects: Seq[Project]): Future[Seq[Int]] = {
    val now = Instant.now()
    val adminId = users.find(_.email == "admin@test.com").flatMap(_.id).get
    val testerId = users.find(_.email == "tester@test.com").flatMap(_.id).get

    val entries = projects.flatMap { project =>
      // admin + tester for each project
      val adminEntry = UserProject(None, adminId, project.id.get, UserProjectRole.owner, None, now)
      val testerEntry = UserProject(None, testerId, project.id.get, UserProjectRole.member, Some(adminId), now)
      Seq(adminEntry, testerEntry)
    } ++ {
      // plus random assignments: for variety, add 1-3 random users to some projects
      val extra = projects.flatMap { project =>
        rnd.shuffle(users.filter(u => u.email != "admin@test.com" && u.email != "tester@test.com")).take(rnd.nextInt(3)).flatMap(_.id).map { uid =>
          UserProject(None, uid, project.id.get, UserProjectRole.member, Some(adminId), now)
        }
      }
      extra
    }

    projectRepository.insertUserBatchIntoProject(entries)
  }

  private def seedColumns(projects: Seq[Project]): Future[Seq[Column]] = {
    val now = Instant.now()

    val cols = projects.flatMap { project =>
      DEFAULT_COLUMNS.zipWithIndex.map { case (name, idx) =>
        Column(
          id = None,
          projectId = project.id.get,
          name = name,
          position = (idx + 1) * 1000,
          createdAt = now,
          updatedAt = now,
          status = ColumnStatus.active
        )
      }
    }

    columnRepository.insertColumnBatch(cols)
  }

  private def seedTasksAndAssignments(columns: Seq[Column], users: Seq[User]): Future[Unit] = {
    val now = Instant.now()
    val testerIdOpt = users.find(_.email == "tester@test.com").flatMap(_.id)
    val userIds = users.flatMap(_.id)
    require(userIds.nonEmpty, "No users to assign tasks to")

    // group columns by projectId for easy access
    val projectToColumns: Map[Int, Seq[Column]] =
      columns.groupBy(_.projectId)

    // generate all tasks with random assignments
    val allSeeds: Seq[(Task, Int)] = projectToColumns.toSeq.flatMap { case (projectId, cols) =>
      (1 to TOTAL_TASKS_PER_PROJECT ).map { i =>
        val col = cols(rnd.nextInt(cols.size)) // random 1 column in project
        val assignTo =
          if (rnd.nextDouble() < PCT_ASSIGNED_TO_TESTER) {
            testerIdOpt.get
          } else {
            userIds(rnd.nextInt(userIds.size))
          }
        val createdBy = userIds(rnd.nextInt(userIds.size))
        val task = Task(
          columnId = col.id.get,
          name = s"Seed task $projectId-$i",
          description = Some(s"Auto generated task #$i for project $projectId"),
          position = i * 1000,
          createdBy = Some(createdBy),
          updatedBy = Some(createdBy),
          createdAt = now,
          updatedAt = now
        )
        (task, assignTo)
      }
    }

    // insert in batches to avoid too large transactions
    allSeeds.grouped(TASK_BATCH).foldLeft(Future.successful(())) { (accF, chunk) =>
      accF.flatMap { _ =>
        val start = System.nanoTime()
        val tasksChunk = chunk.map(_._1)
        taskRepository.insertTaskBatch(tasksChunk).flatMap { insertedIds =>
          val assignedEntries = insertedIds.zip(chunk.map(_._2)).map { case (taskId, assigneeId) =>
            UserTask(None, taskId, assigneeId, None, now)
          }
          taskRepository.insertUserBatchIntoTask(assignedEntries).map { _ =>
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            Logger("application").info(s"Inserted task batch of ${chunk.size} tasks in ${elapsedMs} ms")
          }
        }
      }
    }
  }


}
