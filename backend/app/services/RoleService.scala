package services

import repositories.{RoleRepository, UserRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RoleService @Inject()(
   userRepository: UserRepository,
   roleRepository: RoleRepository
)(implicit ec: ExecutionContext) {

    /**
     * Get user roles by user ID
     */
    def getUserRole(userId: Int): Future[Option[String]] = {
        for {
            user ← userRepository.findById(userId)
            role ← user.flatMap(_.roleId) match {
                case Some(roleId) ⇒ roleRepository.findByRoleId(roleId)
                case None ⇒ Future.successful(None)
            }
        } yield role.map(_.name)
    }

}