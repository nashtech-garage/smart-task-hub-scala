package services

import dto.request.auth.RegisterUserRequest
import models.entities.{Role, User}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.scalatest.RecoverMethods.recoverToExceptionIf
import org.scalatestplus.play._
import org.scalatestplus.mockito.MockitoSugar
import repositories.{RoleRepository, UserRepository}

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import com.github.t3hnar.bcrypt._

class AuthServiceSpec extends PlaySpec with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "AuthService#registerUser" should {

    "register successfully when email does not exist and role exists" in {
      val mockUserRepo = mock[UserRepository]
      val mockRoleRepo = mock[RoleRepository]

      val request = RegisterUserRequest("Alice", "alice@example.com", "pass123")

      when(mockUserRepo.findByEmail(request.email)).thenReturn(Future.successful(None))
      when(mockRoleRepo.findByRoleName("user")).thenReturn(Future.successful(Some(Role(Some(1), "user"))))
      when(mockUserRepo.create(any[User])).thenAnswer(
        (invocation: InvocationOnMock) => {
          val user = invocation.getArgument(0).asInstanceOf[User]
          Future.successful(user.copy(id = Some(1)))
        }
      )

      val service = new AuthService(mockUserRepo, mockRoleRepo)

      val resultFut = service.registerUser(request)

      resultFut.map { user =>
        user.id mustBe Some(1)
        user.name mustBe "Alice"
        user.email mustBe "alice@example.com"
        user.roleId mustBe 1
        user.password.length must be > 0 // hashed password
      }
    }

    "fail when email already exists" in {
      val mockUserRepo = mock[UserRepository]
      val mockRoleRepo = mock[RoleRepository]

      val request = RegisterUserRequest("Bob", "bob@example.com", "abc123")

      when(mockUserRepo.findByEmail(request.email))
          .thenReturn(Future.successful(Some(User(Some(99), "Bob", "bob@example.com", "hashed", None, Some(1), LocalDateTime.now()))))

      val service = new AuthService(mockUserRepo, mockRoleRepo)

      recoverToExceptionIf[RuntimeException] {
        service.registerUser(request)
      }.map { ex =>
        ex.getMessage must include("Email already exists")
      }
    }

    "fail when role 'user' not found" in {
      val mockUserRepo = mock[UserRepository]
      val mockRoleRepo = mock[RoleRepository]

      val request = RegisterUserRequest("Charlie", "charlie@example.com", "qwerty1")

      when(mockUserRepo.findByEmail(request.email)).thenReturn(Future.successful(None))
      when(mockRoleRepo.findByRoleName("user")).thenReturn(Future.successful(None))

      val service = new AuthService(mockUserRepo, mockRoleRepo)

      recoverToExceptionIf[RuntimeException] {
        service.registerUser(request)
      }.map { ex =>
        ex.getMessage must include("Role 'user' not found")
      }
    }
  }

  "AuthService#authenticateUser" should {

    "return Some(UserToken) when email and password are correct" in {
      val mockUserRepo = mock[UserRepository]
      val mockRoleRepo = mock[RoleRepository]

      val plainPassword = "securePass123"
      val hashedPassword = plainPassword.bcryptSafeBounded.get
      val user = User(Some(10), "Diana", "diana@example.com", hashedPassword, None, Some(1), LocalDateTime.now())

      when(mockUserRepo.findByEmail("diana@example.com")).thenReturn(Future.successful(Some(user)))

      val service = new AuthService(mockUserRepo, mockRoleRepo)

      service.authenticateUser("diana@example.com", plainPassword).map { maybeToken =>
        maybeToken mustBe defined
        val token = maybeToken.get
        token.userId mustBe 10
        token.email mustBe "diana@example.com"
        token.name mustBe "Diana"
      }
    }

    "return None when password is incorrect" in {
      val mockUserRepo = mock[UserRepository]
      val mockRoleRepo = mock[RoleRepository]

      val correctPassword = "rightPass123"
      val wrongPassword = "wrongPass"
      val hashedPassword = correctPassword.bcryptSafeBounded.get
      val user = User(Some(5), "Eve", "eve@example.com", hashedPassword, None, Some(1), LocalDateTime.now())

      when(mockUserRepo.findByEmail("eve@example.com")).thenReturn(Future.successful(Some(user)))

      val service = new AuthService(mockUserRepo, mockRoleRepo)

      service.authenticateUser("eve@example.com", wrongPassword).map { maybeToken =>
        maybeToken mustBe empty
      }
    }

    "return None when email is not found" in {
      val mockUserRepo = mock[UserRepository]
      val mockRoleRepo = mock[RoleRepository]

      when(mockUserRepo.findByEmail("ghost@example.com")).thenReturn(Future.successful(None))

      val service = new AuthService(mockUserRepo, mockRoleRepo)

      service.authenticateUser("ghost@example.com", "anyPassword").map { maybeToken =>
        maybeToken mustBe empty
      }
    }
  }
}
