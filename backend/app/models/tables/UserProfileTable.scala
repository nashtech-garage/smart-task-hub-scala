package models.tables

import models.entities.UserProfile
import play.api.db.slick.HasDatabaseConfig
import slick.jdbc.PostgresProfile
import java.time.LocalDateTime

trait UserProfileTable { self: HasDatabaseConfig[PostgresProfile] =>
  import profile.api._

  class UserProfileTable(tag: Tag) extends Table[UserProfile](tag, "user_profiles") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id")
    def userLanguage = column[String]("user_language")
    def themeMode = column[String]("theme_mode")
    def createdAt = column[LocalDateTime]("created_at")
    def updatedAt = column[LocalDateTime]("updated_at")

    def * = (
      id,
      userId,
      userLanguage,
      themeMode,
      createdAt,
      updatedAt
    ) <> (
      { case (id, userId, userLanguage, themeMode, createdAt, updatedAt) =>
          UserProfile(id, userId, userLanguage, themeMode, createdAt, updatedAt)
      },
      (up: UserProfile) => Some((up.id, up.userId, up.userLanguage, up.themeMode, up.createdAt, up.updatedAt))
    )

    private def userFk = foreignKey(
      "user_profile_user_fk",
      userId,
      TableQuery[UserTable]
    )(_.id, onDelete = ForeignKeyAction.Cascade)

    private def userIdIdx = index("user_profile_user_id_idx", userId, unique = true)
  }

  lazy val userProfiles = TableQuery[UserProfileTable]
}