package exception

import play.api.http.Status

case class NotFoundException(message: String = "Not Found", statusCode: Int = Status.NOT_FOUND)
  extends RuntimeException(message)
