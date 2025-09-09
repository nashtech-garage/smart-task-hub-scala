package controllers

import org.mockito.ArgumentMatchers._
import org.mockito.ArgumentMatchersSugar
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class UrlPreviewControllerSpec extends PlaySpec with MockitoSugar {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    def mkController(wsClient: WSClient) = {
        new UrlPreviewController(
            stubControllerComponents(),
            wsClient
        )
    }

    "UrlPreviewController.preview" should {

        "return BadRequest when url parameter is missing" in {
            val ws = mock[WSClient]
            val controller = mkController(ws)

            val request = FakeRequest(GET, "/preview")
            val result = controller.preview()(request)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) must include("URL parameter is required")
        }

        "return BadRequest when URL is invalid" in {
            val ws = mock[WSClient]
            val controller = mkController(ws)

            val request = FakeRequest(GET, "/preview?url=not-a-valid-url")
            val result = controller.preview()(request)

            status(result) mustBe BAD_REQUEST
            contentAsString(result) must include("Invalid URL")
        }

        "return preview data when JsonLink returns valid JSON" in {
            val ws = mock[WSClient]
            val wsRequest = mock[WSRequest]
            val wsResponse = mock[WSResponse]

            val json = Json.obj(
                "title" -> "Example Title",
                "description" -> "Example Description",
                "images" -> Json.arr("https://example.com/image.jpg"),
                "domain" -> "example.com",
                "favicon" -> "https://example.com/favicon.ico"
            )

            when(ws.url(contains("jsonlink.io"))).thenReturn(wsRequest)
            when(wsRequest.withRequestTimeout(any())).thenReturn(wsRequest)
            when(wsRequest.get()).thenReturn(Future.successful(wsResponse))
            when(wsResponse.status).thenReturn(200)
            when(wsResponse.json).thenReturn(json)

            val controller = mkController(ws)

            val encodedUrl = java.net.URLEncoder.encode("https://example.com", "UTF-8")
            val request = FakeRequest(GET, s"/preview?url=$encodedUrl")
            val result = controller.preview()(request)

            status(result) mustBe OK
            val body = contentAsJson(result)
            (body \ "title").as[String] mustBe "Example Title"
            (body \ "description").as[String] mustBe "Example Description"
        }

        "fall back to direct HTML scraping when JsonLink fails" in {
            val ws = mock[WSClient]
            val jsonLinkRequest = mock[WSRequest]
            val directRequest = mock[WSRequest]
            val wsResponse = mock[WSResponse]

            // First call (JsonLink) fails
            when(ws.url(contains("jsonlink.io"))).thenReturn(jsonLinkRequest)
            when(jsonLinkRequest.withRequestTimeout(any())).thenReturn(jsonLinkRequest)
            when(jsonLinkRequest.get()).thenReturn(Future.failed(new RuntimeException("fail")))

            // Second call (direct scraping) returns HTML - use separate mock
            val html = """<html><head>
                         |<title>Fallback Title</title>
                         |<meta name="description" content="Fallback Desc"/>
                         |<link rel="icon" href="https://example.com/favicon.ico" />
                         |</head></html>""".stripMargin

            when(ws.url(ArgumentMatchersSugar.eqTo("https://example.com"))).thenReturn(directRequest)
            when(directRequest.withRequestTimeout(any())).thenReturn(directRequest)
            when(directRequest.withFollowRedirects(true)).thenReturn(directRequest)
            when(directRequest.get()).thenReturn(Future.successful(wsResponse))
            when(wsResponse.status).thenReturn(200)
            when(wsResponse.body).thenReturn(html)

            val controller = mkController(ws)
            val encodedUrl = java.net.URLEncoder.encode("https://example.com", "UTF-8")
            val result = controller.preview()(FakeRequest(GET, s"/preview?url=$encodedUrl"))

            status(result) mustBe OK
            val body = contentAsJson(result)

            // Check the extracted values
            (body \ "title").asOpt[String] mustBe Some("Fallback Title")
            (body \ "description").asOpt[String] mustBe Some("Fallback Desc")
            (body \ "url").as[String] mustBe "https://example.com"
            (body \ "siteName").asOpt[String] mustBe Some("example.com")
            (body \ "favicon").asOpt[String] mustBe Some("https://example.com/favicon.ico")
        }

        "return fallback preview when all fetch methods fail" in {
            val ws = mock[WSClient]
            val wsRequest = mock[WSRequest]

            when(ws.url(any[String])).thenReturn(wsRequest)
            when(wsRequest.withRequestTimeout(any())).thenReturn(wsRequest)
            when(wsRequest.withFollowRedirects(any())).thenReturn(wsRequest)
            when(wsRequest.get()).thenReturn(Future.failed(new RuntimeException("fail")))

            val controller = mkController(ws)
            val encodedUrl = java.net.URLEncoder.encode("https://example.com", "UTF-8")
            val result = controller.preview()(FakeRequest(GET, s"/preview?url=$encodedUrl"))

            status(result) mustBe OK
            val body = contentAsJson(result)
            (body \ "title").as[String] mustBe "example.com"
            (body \ "description").as[String] must include("Click to visit this website")
        }
    }
}