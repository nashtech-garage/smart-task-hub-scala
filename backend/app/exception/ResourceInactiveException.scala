package exception

import play.api.http.Status

case class ResourceInactiveException(message: String = "Resource Inactive", statusCode: Int = Status.CONFLICT)
  extends RuntimeException(message)
