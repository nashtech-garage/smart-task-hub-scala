package services

import javax.inject._
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}
import controllers.UrlPreviewData

import java.net.URL
import org.jsoup.Jsoup
import play.api.Configuration

import scala.concurrent.duration._

@Singleton
class UrlPreviewService @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {

    def fetchPreview(url: String): Future[UrlPreviewData] = {
        fetchFromMultipleSources(url)
    }

    private def fetchFromMultipleSources(url: String): Future[UrlPreviewData] = {
            val apiKey = config.get[String]("linkpreview.api.key")
        // Try LinkPreview.net first (if you have an API key)
         fetchFromLinkPreviewNet(url, apiKey).recoverWith {
           case _ =>
            fetchFromJsonLink(url).recoverWith {
                case _ => fetchFromOpenGraph(url)
            }.recoverWith {
                case _ => Future.successful(createBasicPreview(url))
            }
         }
    }

    private def fetchFromLinkPreviewNet(url: String, apiKey: String): Future[UrlPreviewData] = {
        val apiUrl = s"https://api.linkpreview.net/?key=$apiKey&q=${java.net.URLEncoder.encode(url, "UTF-8")}"

        ws.url(apiUrl)
            .withRequestTimeout(10.seconds)
            .get()
            .map { response =>
                val json = response.json
                UrlPreviewData(
                    url = url,
                    title = (json \ "title").asOpt[String],
                    description = (json \ "description").asOpt[String],
                    image = (json \ "image").asOpt[String],
                    siteName = (json \ "url").asOpt[String].map(new URL(_).getHost),
                    favicon = Some(s"https://www.google.com/s2/favicons?domain=${new URL(url).getHost}&sz=32")
                )
            }
    }

    private def fetchFromJsonLink(url: String): Future[UrlPreviewData] = {
        val apiUrl = s"https://jsonlink.io/api/extract?url=${java.net.URLEncoder.encode(url, "UTF-8")}"

        ws.url(apiUrl)
            .withRequestTimeout(8.seconds)
            .get()
            .map { response =>
                if (response.status == 200) {
                    val json = response.json
                    UrlPreviewData(
                        url = url,
                        title = (json \ "title").asOpt[String].filter(_.nonEmpty),
                        description = (json \ "description").asOpt[String].filter(_.nonEmpty),
                        image = (json \ "images").asOpt[Seq[String]].flatMap(_.headOption)
                            .orElse((json \ "image").asOpt[String]).filter(_.nonEmpty),
                        siteName = (json \ "domain").asOpt[String].filter(_.nonEmpty)
                            .orElse(Some(new URL(url).getHost)),
                        favicon = (json \ "favicon").asOpt[String].filter(_.nonEmpty)
                            .orElse(Some(s"https://www.google.com/s2/favicons?domain=${new URL(url).getHost}&sz=32"))
                    )
                } else {
                    throw new Exception(s"JsonLink API returned status: ${response.status}")
                }
            }
    }

    private def fetchFromOpenGraph(url: String): Future[UrlPreviewData] = {
        ws.url(url)
            .withRequestTimeout(8.seconds)
            .withFollowRedirects(true)
            .get()
            .map { response =>
                if (response.status == 200) {
                    parseHtmlForPreview(url, response.body)
                } else {
                    throw new Exception(s"Failed to fetch URL: ${response.status}")
                }
            }
    }

    private def parseHtmlForPreview(url: String, html: String): UrlPreviewData = {
        val doc = Jsoup.parse(html)
        val urlObj = new URL(url)

        val title = Option(doc.select("meta[property=og:title]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Option(doc.select("meta[name=twitter:title]").attr("content")).filter(_.nonEmpty))
            .orElse(Option(doc.title()).filter(_.nonEmpty))

        val description = Option(doc.select("meta[property=og:description]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Option(doc.select("meta[name=twitter:description]").attr("content")).filter(_.nonEmpty))
            .orElse(Option(doc.select("meta[name=description]").attr("content")).filter(_.nonEmpty))

        val image = Option(doc.select("meta[property=og:image]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Option(doc.select("meta[name=twitter:image]").attr("content")).filter(_.nonEmpty))
            .map(img => if (img.startsWith("http")) img else s"${urlObj.getProtocol}://${urlObj.getHost}$img")

        val siteName = Option(doc.select("meta[property=og:site_name]").attr("content"))
            .filter(_.nonEmpty)
            .orElse(Some(urlObj.getHost))

        val favicon = Option(doc.select("link[rel=icon]").attr("href"))
            .orElse(Option(doc.select("link[rel='shortcut icon']").attr("href")))
            .map(fav => if (fav.startsWith("http")) fav else s"${urlObj.getProtocol}://${urlObj.getHost}$fav")
            .orElse(Some(s"https://www.google.com/s2/favicons?domain=${urlObj.getHost}&sz=32"))

        UrlPreviewData(
            url = url,
            title = title,
            description = description,
            image = image,
            siteName = siteName,
            favicon = favicon
        )
    }

    private def createBasicPreview(url: String): UrlPreviewData = {
        val urlObj = new URL(url)
        UrlPreviewData(
            url = url,
            title = Some(urlObj.getHost),
            description = Some("Click to visit this website"),
            siteName = Some(urlObj.getHost),
            favicon = Some(s"https://www.google.com/s2/favicons?domain=${urlObj.getHost}&sz=32")
        )
    }
}