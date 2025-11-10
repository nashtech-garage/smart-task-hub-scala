package exception

import play.api.http.Status

/**
 * Base application exception with an associated HTTP status code.
 * Specific exceptions extend this so handlers can map them to proper HTTP responses.
 */
case class AppException(message: String, val statusCode: Int = Status.BAD_REQUEST) extends RuntimeException(message)
