package controllers

import play.api.mvc.{AbstractController, ControllerComponents}
import services.UserService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UserController @Inject()(
                                cc: ControllerComponents,
                                userService: UserService
                              )(implicit ec: ExecutionContext) extends AbstractController(cc) {


}
