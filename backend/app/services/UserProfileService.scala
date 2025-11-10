package services

import javax.inject.{Inject, Singleton}
import models.entities.UserProfile
import repositories.UserProfileRepository
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserProfileService @Inject()(
  userProfileRepository: UserProfileRepository
)(implicit ec: ExecutionContext) {

  def createProfile(newProfile: UserProfile): Future[UserProfile] = {
    userProfileRepository.create(newProfile)
  }

  def updateProfile(userProfile: UserProfile): Future[Option[UserProfile]] = {
    userProfileRepository.update(userProfile).flatMap {
      case 0 => Future.successful(None)
      case _ => userProfileRepository.findById(userProfile.id.get)
    }
  }

  def getUserProfile(userId: Int): Future[Option[UserProfile]] = {
    userProfileRepository.findByUserId(userId)
  }
}