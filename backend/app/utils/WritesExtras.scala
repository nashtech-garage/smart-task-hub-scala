package utils

import play.api.libs.json.{Json, Writes}


/**
 * Contains extra implicit Writes that are not provided by Play JSON by default.
 *
 * Example:
 *   - Writes[Unit]: Allows serializing Unit (i.e., no data) as an empty JSON
 *     object `{}`.
 *
 * This can be useful for generic response wrappers like ApiResponse[Unit],
 * where you want to return success/failure without any actual data.
 *
 * Example usage:
 * {{{
 *   import utils.json.WritesExtras._
 *   val response: ApiResponse[Unit] = ApiResponse(
 *     success = false,
 *     message = "Validation failed",
 *     errors = Some(fieldErrors)
 *   )
 *    Future.successful(BadRequest(Json.toJson(response)))
 * }}}
 */
object WritesExtras {
  implicit val unitWrites: Writes[Unit] = (_: Unit) => Json.obj()
}