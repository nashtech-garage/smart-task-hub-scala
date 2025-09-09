package services

import dto.request.auth.RegisterUserRequest
import models.entities.User
import repositories.{RoleRepository, UserRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import com.github.t3hnar.bcrypt._
import dto.response.auth.AuthResponse
import exception.AppException
import play.api.http.Status

import scala.util.{Failure, Success}

/**
 * Service responsible for authentication-related operations.
 *
 * @param userRepository Repository for user data access.
 * @param roleRepository Repository for role data access.
 * @param ec Execution context for asynchronous operations.
 */
@Singleton
class AuthService@Inject()(
                            userRepository: UserRepository,
                            roleRepository: RoleRepository
                          )(implicit ec: ExecutionContext) {

  /**
   * Registers a new user.
   *
   * Checks if the email already exists, hashes the password, assigns the 'user' role,
   * and creates a new user record.
   *
   * @param request The registration request containing user details.
   * @return A Future containing the created User.
   * @throws RuntimeException if the email already exists or the 'user' role is not found.
   */
  def registerUser(request: RegisterUserRequest): Future[User] = {
    // Check if email existed
    userRepository.findByEmail(request.email).flatMap {
      case Some(_) =>
      throw AppException(
        message = "Email already exists",
        statusCode = Status.CONFLICT
      )
      case None =>
        roleRepository.findByRoleName("user").flatMap {
          case Some(role) =>
            val hashedPassword = request.password.bcryptSafeBounded.get

            val newUser = User(
              name = request.name,
              email = request.email,
              password = hashedPassword,
              roleId = role.id
            )
            userRepository.create(newUser)

          case None =>
            throw AppException(
              message = "Role 'user' not found", statusCode = Status.NOT_FOUND
            )
        }
    }
  }

  def authenticateUser(email: String, password: String): Future[Option[UserToken]] = {

      userRepository.findByEmail(email).map {
        case Some(user) ⇒
          password.isBcryptedSafeBounded(user.password) match {
            case Success(true) ⇒
                  Some(UserToken(
                    userId = user.id.get,
                    email = user.email,
                    name = user.name
                  ))
            case Success(false) ⇒
                  None
            case Failure(_) ⇒
                  None
        }
        case None ⇒ None
      }
  }

//  Convert UserToken to AuthResponse
  def userTokenToAuthResponse(userToken: UserToken): AuthResponse = {
    AuthResponse(
      id = userToken.userId.toInt,
      name = userToken.name,
      email = userToken.email
    )
  }
}
