package validations

import dto.response.{ApiResponse, FieldError}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import play.api.mvc.{Result, Results}
import utils.WritesExtras._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Trait that provides a reusable method to validate JSON payloads in Play
 * Framework controllers.
 *
 * This method handles parsing, validation errors, and delegates successful
 * validation to a custom function.
 *
 * Example usage in a controller:
 * {{{
 * class UserController @Inject() (
 *   cc: ControllerComponents,
 *   userService: UserService
 * )(implicit ec: ExecutionContext)
 *   extends AbstractController(cc)
 *     with ValidationHandler {
 *
 *   def createUser: Action[JsValue] = Action.async(parse.json) { request =>
 *     handleJsonValidation[CreateUserRequestDto](request.body) { validatedDto =>
 *       userService.createUser(validatedDto).map { createdUser =>
 *         val response = ApiResponse(
 *           success = true,
 *           message = "User created successfully",
 *           data = Some(Json.toJson(createdUser))
 *         )
 *         Created(Json.toJson(response))
 *       }
 *     }
 *   }
 * }
 * }}}
 */
trait ValidationHandler extends Results {

  /**
   * Validates a JSON payload into a specific type `T` using an implicit
   * `Reads[T]`. If validation passes, the `onSuccess` function is called with
   * the validated object. If validation fails, returns a `BadRequest` with
   * detailed validation error messages.
   *
   * @param jsValue
   *   The incoming JSON value to validate.
   * @param onSuccess
   *   The function to execute if validation is successful.
   * @param reads
   *   Implicit JSON reader for type `T`.
   * @param ec
   *   Implicit execution context for handling Futures.
   * @tparam T
   *   The target type into which the JSON will be validated.
   * @return
   *   A Future[Result], either `BadRequest` with errors or the result of
   *   `onSuccess`.
   */
  def handleJsonValidation[T](
                               jsValue: JsValue
                             )(onSuccess: T => Future[Result])(implicit
                                                               reads: Reads[T],
                                                               ec: ExecutionContext
                             ): Future[Result] = {
    jsValue.validate[T] match {
      case JsSuccess(validatedData, _) =>
        // If validation succeeds, proceed with the provided success function
        onSuccess(validatedData)

      case JsError(errors) =>
        // If validation fails, extract error messages from JsError
        val fieldErrors = errors.flatMap {
          case (path, validationErrors) =>
            validationErrors.map {
              err =>
                FieldError(path.toString().stripPrefix("/"), err.message)
            }
        }.toList

        val response: ApiResponse[Unit] = ApiResponse(
          message = "Validation failed",
          errors = Some(fieldErrors)
        )

        // Return 400 BadRequest with detailed error response
        Future.successful(BadRequest(Json.toJson(response)))
    }
  }
}