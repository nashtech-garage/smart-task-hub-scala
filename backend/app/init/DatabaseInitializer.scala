package init

import models.entities.{Role, User}
import repositories.{RoleRepository, UserRepository}
import play.api.{Configuration, Logger, Logging}
import com.github.t3hnar.bcrypt._

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DatabaseInitializer @Inject()(
                                     userRepository: UserRepository,
                                     roleRepository: RoleRepository,
                                     config: Configuration,
                                     dataSeeder: DataSeeder
                                   )(implicit ec: ExecutionContext) {

  def initializeDatabase(): Future[Unit] = {
    Logger("application").info("Starting database initialization...")

    for {
      _ <- createDefaultAdmin()
      _ <- dataSeeder.seedAll()
    } yield {
      Logger("application").info("Database initialization completed")
    }
  }

  private def createDefaultAdmin(): Future[Unit] = {
    val defaultAdminEmail = config.getOptional[String]("admin.email")
      .getOrElse("admin@mail.com")
    val defaultAdminPassword = config.getOptional[String]("admin.password")
      .getOrElse("admin123")
    val defaultAdminName = config.getOptional[String]("admin.name")
      .getOrElse("Administrator")

    userRepository.findByEmail(defaultAdminEmail).flatMap {
      case Some(existingUser) =>
        Logger("application").info(s"Admin user already exists: $defaultAdminEmail, skipping creation")
        Future.successful(())
      case None =>
        Logger("application").info(s"Creating default admin user: $defaultAdminEmail")
        createAdminUser(defaultAdminName, defaultAdminEmail, defaultAdminPassword)
    }.recover {
      case ex =>
        Logger("application").error(s"Failed to create default admin user: ${ex.getMessage}", ex)
        throw ex
    }
  }

  private def createAdminUser(name: String, email: String, password: String): Future[Unit] = {
    for {
      adminRole <- roleRepository.findByRoleName("admin").flatMap {
        case Some(role) => Future.successful(role)
        case None => roleRepository.create(Role(name = "admin"))
      }

      hashedPassword = password.bcryptSafeBounded.getOrElse(
        throw new RuntimeException("Failed to hash admin password")
      )
      adminUser = User(
        name = name,
        email = email,
        password = hashedPassword,
        roleId = adminRole.id,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
      )
      _ <- userRepository.create(adminUser)
    } yield {
      Logger("application").info(s"Default admin user created successfully: $email")
    }
  }
}
