package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import services.UserProfileService
import models.entities.UserProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserProfileController @Inject()(
  val controllerComponents: ControllerComponents,
  userProfileService: UserProfileService,
  authenticatedAction: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext) extends BaseController {

  implicit val userProfileWrites: OWrites[UserProfile] = Json.writes[UserProfile]

  // Request DTO for partial updates
  private case class UpdateUserProfileRequest(userLanguage: Option[String], themeMode: Option[String])
  private object UpdateUserProfileRequest {
    implicit val reads: Reads[UpdateUserProfileRequest] = Json.reads[UpdateUserProfileRequest]
  }

  def getUserProfile: Action[AnyContent] = authenticatedAction.async { request =>
    userProfileService.getUserProfile(request.userToken.userId).map {
      case Some(profile) => Ok(Json.toJson(profile))
      case None => NotFound
    }
  }

  def updateProfile: Action[JsValue] = authenticatedAction.async(parse.json) { request =>
    request.body.validate[UpdateUserProfileRequest].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      dto => {
        userProfileService.getUserProfile(request.userToken.userId).flatMap {
          case None => Future.successful(NotFound)
          case Some(existing) =>
            val updated = existing.copy(
              userLanguage = dto.userLanguage.getOrElse(existing.userLanguage),
              themeMode = dto.themeMode.getOrElse(existing.themeMode),
              updatedAt = java.time.LocalDateTime.now()
            )
            userProfileService.updateProfile(updated).map {
              case Some(profile) => Ok(Json.toJson(profile))
              case None => InternalServerError(Json.obj("error" -> "Failed to update profile"))
            }
        }
      }
    )
  }
}