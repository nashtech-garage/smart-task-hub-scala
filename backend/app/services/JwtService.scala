package services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.{JWTVerificationException, JWTCreationException}
import play.api.Configuration
import play.api.libs.json.Json

import java.util.Date
import scala.concurrent.duration._
import javax.inject.{Inject, Singleton}
import scala.util.Try

case class UserToken(userId: Int, name: String, email: String)

object UserToken {
    implicit val format = Json.format[UserToken]
}

case class JwtConfig (
    secret: String,
    expirationHours: Int,
    issuer: String
)

case class CookieConfig(
   name: String,
   maxAge: Int,
   secure: Boolean,
   httpOnly: Boolean,
   sameSite: String
)

@Singleton
class JwtService @Inject()(config: Configuration) {

//    Load JWT config from app.conf
    private val jwtConfig = JwtConfig(
        secret = config.get[String]("jwt.secret"),
        expirationHours = config.get[Int]("jwt.expiration.hours"),
        issuer = config.get[String]("jwt.issuer")
    )

    val cookieConfig = CookieConfig(
        name = config.get[String]("cookie.name"),
        maxAge = config.get[Int]("cookie.maxAge"),
        secure = config.get[Boolean]("cookie.secure"),
        httpOnly = config.get[Boolean]("cookie.httpOnly"),
        sameSite = config.get[String]("cookie.sameSite")
    )

    private val algorithm = Algorithm.HMAC256(jwtConfig.secret)
    private val expiration = jwtConfig.expirationHours.hours

    def generateToken(userToken: UserToken): Try[String] = {
        Try {
            val now = new Date()
            val expiresAt = new Date(now.getTime + expiration.toMillis)

            JWT.create()
                .withSubject(userToken.userId.toString)
                .withClaim("name", userToken.name)
                .withClaim("email", userToken.email)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withIssuer(jwtConfig.issuer)
                .sign(algorithm)
        }.recover {
            case ex: JWTCreationException =>
                throw new Exception(s"Failed to create JWT token: ${ex.getMessage}")
            case ex =>
                throw new Exception(s"Unexpected error creating token: ${ex.getMessage}")
        }
    }

    def validateToken(token: String): Try[UserToken] = {
        Try {
            var verifier = JWT.require(algorithm)
                .withIssuer(jwtConfig.issuer)
                .build()

            val decodedJWT = verifier.verify(token)

            var userId = decodedJWT.getSubject
            var email = decodedJWT.getClaim("email").asString()
            var name = decodedJWT.getClaim("name").asString()

            if (userId == null || userId.isEmpty) {
                throw new Exception("Invalid token: missing user ID")
            }
            if (email == null || email.isEmpty) {
                throw new Exception("Invalid token: missing email")
            }
            if (name == null || name.isEmpty) {
                throw new Exception("Invalid token: missing name")
            }

            UserToken(userId.toInt, name, email)
        }.recover {
            case ex: JWTVerificationException =>
                throw new Exception(s"Token verification failed: ${ex.getMessage}")
            case ex =>
                throw new Exception(s"Token validation error: ${ex.getMessage}")
        }
    }

    def isTokenValid(token: String): Boolean = {
        validateToken(token).isSuccess
    }

    def refreshToken(userToken: UserToken): Try[String] = {
        generateToken(userToken)
    }
}

