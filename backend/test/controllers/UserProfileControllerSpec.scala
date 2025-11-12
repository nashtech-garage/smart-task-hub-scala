package controllers

import dto.request.profile.UpdateUserProfileRequest
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, CREATED, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.Helpers.{GET, POST, PUT, contentAsJson, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Configuration}
import services.{JwtService, UserToken}

import scala.concurrent.ExecutionContext

class UserProfileControllerSpec extends PlaySpec
  with Injecting
  with GuiceOneAppPerSuite
  with ScalaFutures
  with BeforeAndAfterAll {

  override implicit def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(
        "config.resource" -> "application.test.conf",
        "slick.dbs.default.db.url"
          -> s"jdbc:h2:mem:user_profile;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_UPPER=false"
      )
      .build()
  }

  implicit lazy val system: ActorSystem = app.injector.instanceOf[ActorSystem]
  implicit lazy val mat: Materializer = app.injector.instanceOf[Materializer]
  implicit lazy val ec: ExecutionContext =
    app.injector.instanceOf[ExecutionContext]
  lazy val controller: WebSocketController =
    app.injector.instanceOf[WebSocketController]

  lazy val config: Configuration = app.configuration
  lazy val defaultAdminEmail: String =
    config.getOptional[String]("admin.email").getOrElse("admin@mail.com")
  lazy val defaultAdminName: String =
    config.getOptional[String]("admin.name").getOrElse("Administrator")
  lazy val cookieName: String =
    config.getOptional[String]("cookie.name").getOrElse("auth_token")

  def fakeToken: String = {
    val jwtService = inject[JwtService]
    jwtService
      .generateToken(UserToken(1, defaultAdminName, defaultAdminEmail))
      .getOrElse(throw new RuntimeException("JWT token not generated"))
  }

  "UserProfileControllerSpec" should {
    "create user profile successfully" in {
      val body = Json.toJson(UpdateUserProfileRequest(Some("en"), Some("dark")))
      val request = FakeRequest(POST, "/api/user/profile")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String] mustBe "User profile created successfully"
      (contentAsJson(result) \ "data" \ "id").as[Int] mustBe 1
      (contentAsJson(result) \ "data" \ "userId").as[Int] mustBe 1
      (contentAsJson(result) \ "data" \ "userLanguage").as[String] mustBe "en"
      (contentAsJson(result) \ "data" \ "themeMode").as[String] mustBe "dark"
    }

    "return BadRequest when creating user profile with invalid data" in {
      val invalidJson = Json.parse("""{ "userLanguage": 123, "themeMode": false }""")
      val request = FakeRequest(POST, "/api/user/profile")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(invalidJson)

      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
    }

    "get user profile successfully" in {
      val request = FakeRequest(GET, "/api/user/profile")
        .withCookies(Cookie(cookieName, fakeToken))

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "User profile retrieved successfully"
      (contentAsJson(result) \ "data" \ "id").as[Int] mustBe 1
      (contentAsJson(result) \ "data" \ "userId").as[Int] mustBe 1
      (contentAsJson(result) \ "data" \ "userLanguage").as[String] mustBe "en"
      (contentAsJson(result) \ "data" \ "themeMode").as[String] mustBe "dark"
    }

    "return NotFound when getting non-existing user profile" in {
      val jwtService = inject[JwtService]
      val token = jwtService
        .generateToken(UserToken(999, "NonExistent", "non.exist@mail.com"))
        .getOrElse(throw new RuntimeException("JWT token not generated"))
      val request = FakeRequest(GET, "/api/user/profile")
        .withCookies(Cookie(cookieName, token))
      val result = route(app, request).get
      status(result) mustBe NOT_FOUND
    }

    "update user profile successfully" in {
      val body = Json.toJson(UpdateUserProfileRequest(Some("fr"), Some("light")))
      val request = FakeRequest(PUT, "/api/user/profile")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(body)

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "User profile updated successfully"
      (contentAsJson(result) \ "data" \ "id").as[Int] mustBe 1
      (contentAsJson(result) \ "data" \ "userLanguage").as[String] mustBe "fr"
      (contentAsJson(result) \ "data" \ "themeMode").as[String] mustBe "light"
    }

    "return BadRequest when updating user profile with invalid data" in {
      val invalidJson = Json.parse("""{ "userLanguage": 123, "themeMode": false }""")
      val request = FakeRequest(PUT, "/api/user/profile")
        .withCookies(Cookie(cookieName, fakeToken))
        .withBody(invalidJson)

      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
    }

    "return NotFound when updating non-existing user profile" in {
      val jwtService = inject[JwtService]
      val token = jwtService
        .generateToken(UserToken(999, "NonExistent", "non.exist@mail.com"))
        .getOrElse(throw new RuntimeException("JWT token not generated"))
      val body = Json.toJson(UpdateUserProfileRequest(Some("es"), Some("dark")))
      val request = FakeRequest(PUT, "/api/user/profile")
        .withCookies(Cookie(cookieName, token))
        .withBody(body)
      val result = route(app, request).get
      status(result) mustBe NOT_FOUND
    }
  }
}
