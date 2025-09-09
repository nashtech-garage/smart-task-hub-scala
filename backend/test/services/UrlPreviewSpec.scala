package services

import com.typesafe.config.ConfigFactory
import controllers.UrlPreviewData
import org.scalatest.OptionValues._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class UrlPreviewServiceSpec
    extends AsyncWordSpec
        with Matchers
        with MockitoSugar
        with ScalaFutures {

    implicit val ec: ExecutionContext = ExecutionContext.global

    val mockWsClient: WSClient = mock[WSClient]
    val mockLinkPreviewRequest: WSRequest = mock[WSRequest]
    val mockJsonlinkRequest: WSRequest = mock[WSRequest]
    val mockOgRequest: WSRequest = mock[WSRequest]

    val mockLinkPreviewResponse: WSResponse = mock[WSResponse]
    val mockJsonlinkResponse: WSResponse = mock[WSResponse]
    val mockOgResponse: WSResponse = mock[WSResponse]

    val testUrl = "https://example.com"

    // Service with API key (realistic, from test.conf)
    val config: Configuration = Configuration(ConfigFactory.load("application.test.conf"))
    val serviceWithApiKey = new UrlPreviewService(mockWsClient, config)

    // Service without API key (dummy config with empty key)
    val emptyConfig: Configuration = Configuration(
        ConfigFactory.parseString("linkpreview.api.key = \"\"")
    )
    val serviceWithoutApiKey = new UrlPreviewService(mockWsClient, emptyConfig)

    "UrlPreviewService#fetchPreview" should {

        "return preview data from LinkPreview.net when API key is present" in {
            val json = Json.obj(
                "title" -> "LinkPreview Title",
                "description" -> "LinkPreview Description",
                "image" -> "https://example.com/linkpreview.jpg",
                "url" -> testUrl
            )

            when(
                mockWsClient.url(org.mockito.ArgumentMatchers.contains("linkpreview.net"))
            ).thenReturn(mockLinkPreviewRequest)
            when(mockLinkPreviewRequest.withRequestTimeout(10.seconds))
                .thenReturn(mockLinkPreviewRequest)
            when(mockLinkPreviewRequest.get())
                .thenReturn(Future.successful(mockLinkPreviewResponse))

            when(mockLinkPreviewResponse.json).thenReturn(json)

            serviceWithApiKey.fetchPreview(testUrl).map { result =>
                result.title mustBe Some("LinkPreview Title")
                result.description mustBe Some("LinkPreview Description")
                result.image mustBe Some("https://example.com/linkpreview.jpg")
                result.siteName mustBe Some("example.com")
                result.favicon.value must include("google.com/s2/favicons")
            }
        }

        "fall back to JsonLink when LinkPreview.net fails" in {
            // LinkPreview fails
            when(
                mockWsClient.url(org.mockito.ArgumentMatchers.contains("linkpreview.net"))
            ).thenReturn(mockLinkPreviewRequest)
            when(mockLinkPreviewRequest.withRequestTimeout(10.seconds))
                .thenReturn(mockLinkPreviewRequest)
            when(mockLinkPreviewRequest.get())
                .thenReturn(Future.failed(new RuntimeException("LinkPreview failed")))

            // JsonLink succeeds
            val json = Json.obj(
                "title" -> "JsonLink Title",
                "description" -> "JsonLink Description",
                "images" -> Json.arr("https://example.com/jsonlink.jpg"),
                "domain" -> "example.com",
                "favicon" -> "https://example.com/jsonlink.ico"
            )

            when(
                mockWsClient.url(org.mockito.ArgumentMatchers.contains("jsonlink.io"))
            ).thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.get())
                .thenReturn(Future.successful(mockJsonlinkResponse))

            when(mockJsonlinkResponse.status).thenReturn(200)
            when(mockJsonlinkResponse.json).thenReturn(json)

            serviceWithApiKey.fetchPreview(testUrl).map { result =>
                result.title mustBe Some("JsonLink Title")
                result.description mustBe Some("JsonLink Description")
                result.image mustBe Some("https://example.com/jsonlink.jpg")
                result.favicon mustBe Some("https://example.com/jsonlink.ico")
            }
        }

        "fall back to OpenGraph when JsonLink API fails" in {
            // JsonLink fails
            when(
                mockWsClient.url(org.mockito.ArgumentMatchers.contains("jsonlink.io"))
            ).thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.get())
                .thenReturn(Future.failed(new RuntimeException("JsonLink failed")))

            // OpenGraph succeeds
            when(mockWsClient.url(org.mockito.ArgumentMatchers.eq(testUrl)))
                .thenReturn(mockOgRequest)
            when(mockOgRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockOgRequest)
            when(mockOgRequest.withFollowRedirects(true))
                .thenReturn(mockOgRequest)
            when(mockOgRequest.get())
                .thenReturn(Future.successful(mockOgResponse))

            val html =
                """
                  |<html>
                  |<head>
                  |<title>Fallback Title</title>
                  |<meta property="og:description" content="Fallback description"/>
                  |</head>
                  |<body></body>
                  |</html>
                  |""".stripMargin

            when(mockOgResponse.status).thenReturn(200)
            when(mockOgResponse.body).thenReturn(html)

            serviceWithoutApiKey.fetchPreview(testUrl).map { result =>
                result.title mustBe Some("Fallback Title")
                result.description mustBe Some("Fallback description")
            }
        }

        "return basic preview when all sources fail" in {
            when(mockWsClient.url(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withRequestTimeout(org.mockito.ArgumentMatchers.any()))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.withFollowRedirects(true))
                .thenReturn(mockJsonlinkRequest)
            when(mockJsonlinkRequest.get())
                .thenReturn(Future.failed(new RuntimeException("All sources failed")))

            serviceWithoutApiKey.fetchPreview(testUrl).map { result =>
                result.title mustBe Some("example.com")
                result.description mustBe Some("Click to visit this website")
                result.siteName mustBe Some("example.com")
            }
        }
    }
}
