package exception

import play.api.http.Status

case class BadRequestException(message: String = "Bad Request", statusCode: Int = Status.BAD_REQUEST)
  extends RuntimeException(message)
