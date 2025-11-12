package controllers

import dto.request.profile.UpdateUserProfileRequest
import dto.response.ApiResponse

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import services.UserProfileService
import models.entities.UserProfile

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserProfileController @Inject()(
  val controllerComponents: ControllerComponents,
  userProfileService: UserProfileService,
  authenticatedAction: AuthenticatedActionWithUser
)(implicit ec: ExecutionContext) extends BaseController {

  implicit val userProfileWrites: OWrites[UserProfile] = Json.writes[UserProfile]

  def getUserProfile: Action[AnyContent] = authenticatedAction.async { request =>
    userProfileService.getUserProfile(request.userToken.userId).map {
      case Some(profile) => Ok(Json.toJson(ApiResponse("User profile retrieved successfully", Some(profile))))
      case None => NotFound
    }
  }

  def createProfile: Action[JsValue] = authenticatedAction.async(parse.json) { request =>
    request.body.validate[UpdateUserProfileRequest].fold(
      errors => Future.successful(BadRequest(JsError.toJson(errors))),
      dto => {
        val newProfile = UserProfile(
          userId = request.userToken.userId,
          userLanguage = dto.userLanguage.get,
          themeMode = dto.themeMode.get,
          createdAt = LocalDateTime.now(),
          updatedAt = LocalDateTime.now()
        )
        userProfileService.createProfile(newProfile).map { profile =>
          Created(Json.toJson(ApiResponse("User profile created successfully", Some(profile))))
        }
      }
    )
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
              case Some(profile) => Ok(Json.toJson(ApiResponse("User profile updated successfully", Some(profile))))
              case None => InternalServerError(Json.obj("error" -> "Failed to update profile"))
            }
        }
      }
    )
  }
}