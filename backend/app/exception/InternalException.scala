package exception

import play.api.http.Status

case class InternalErrorException(message: String = "Internal Server Error", statusCode: Int = Status.INTERNAL_SERVER_ERROR)
  extends RuntimeException(message)
