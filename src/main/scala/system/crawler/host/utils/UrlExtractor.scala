package system.crawler.host.utils

import akka.http.scaladsl.model.Uri
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document

class UrlExtractor {
  private val browser = JsoupBrowser()
  private val urlTransformer = new UrlTransformer

  def extractData(htmlBody: String, baseUrl: Uri): Tuple2[String, Set[Uri]] = {
    val document = browser.parseString(htmlBody)
    val bodyText = extractText(document)
    val urls = filterSubUrl(extractRawUrlList(document), baseUrl).toSet
    (bodyText, urls)
  }

  private def extractText(document: Document) = {
    document.body.text
  }

  private def extractRawUrlList(doc: Document) = {
    val items = doc >> elementList("a")
    val href = "href"
    items.filter(_.hasAttr(href)).map(_.attr(href)).map(Uri(_))
  }

  private def filterSubUrl(rawUrls: Seq[Uri], baseUrl: Uri) = {
    val currentPath = baseUrl.toString()
    val basePath = urlTransformer.prepareBasePath(currentPath)

    def isSubUrl(url: Uri) = url match {
      case value if value.isAbsolute && value.toString.contains(basePath) => true
      case value if value.isRelative => true
      case value if value.path.toString.startsWith("#") => false
      case value => false
    }

    def isNotFile(url: Uri) = "(\\/+([^\\.])+(\\.(html|text)|$))".r
      .findFirstIn(url.toString())
      .isDefined

    def convertToFullPath(path: Uri) =
      if (path.isAbsolute) path
      else Uri(urlTransformer.toAbsolutePath(currentPath, path.toString()))

    rawUrls.filter(isSubUrl).map(convertToFullPath).filter(isNotFile)
  }
}
