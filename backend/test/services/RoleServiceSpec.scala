package services

import models.entities.{Role, User}
import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import repositories.{RoleRepository, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

class RoleServiceSpec extends PlaySpec with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val mockUserRepository: UserRepository = mock[UserRepository]
  val mockRoleRepository: RoleRepository = mock[RoleRepository]

  val service = new RoleService(mockUserRepository, mockRoleRepository)

  "RoleService#getUserRole" should {

    "return Some(roleName) when user and role exist" in {
      val userId = 1
      val roleId = 2
      val user = User(
        id = Some(userId),
        name = "user",
        email = "email@example.com",
        roleId = Some(roleId),
        password = "rawPassword"
      )

      when(mockUserRepository.findById(userId)).thenReturn(Future.successful(Some(user)))
      val role = Role(Some(roleId), "user")

      when(mockUserRepository.findById(userId)).thenReturn(Future.successful(Some(user)))
      when(mockRoleRepository.findByRoleId(roleId)).thenReturn(Future.successful(Some(role)))

      val futureResult = service.getUserRole(userId)

      futureResult.map { result =>
        result mustBe Some("user")
      }
    }

    "return None when user does not exist" in {
      val userId = 100

      when(mockUserRepository.findById(userId)).thenReturn(Future.successful(None))

      val futureResult = service.getUserRole(userId)

      futureResult.map { result =>
        result mustBe None
      }
    }

    "return None when user exists but roleId is None" in {
      val userId = 2
      val user = User(Some(userId), "user2", "email2@example.com", "rawPassword")

      when(mockUserRepository.findById(userId)).thenReturn(Future.successful(Some(user)))

      val futureResult = service.getUserRole(userId)

      futureResult.map { result =>
        result mustBe None
      }
    }

    "return None when roleId is present but role not found" in {
      val userId = 3
      val roleId = 99
      val user = User(
        id = Some(userId),
        name = "user3",
        email = "email3@example.com",
        password = "rawPassword",
        roleId = Some(roleId)
      )

      when(mockUserRepository.findById(userId)).thenReturn(Future.successful(Some(user)))
      when(mockRoleRepository.findByRoleId(roleId)).thenReturn(Future.successful(None))

      val futureResult = service.getUserRole(userId)

      futureResult.map { result =>
        result mustBe None
      }
    }

  }
}
