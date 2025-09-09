package exception

import play.api.http.Status

case class AppException(message: String, statusCode: Int = Status.BAD_REQUEST) extends RuntimeException {

}
