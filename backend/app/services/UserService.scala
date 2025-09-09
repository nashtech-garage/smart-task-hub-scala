package services


import repositories.UserRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserService @Inject()(
                             userRepository: UserRepository
                           )(implicit ec: ExecutionContext) {

}
