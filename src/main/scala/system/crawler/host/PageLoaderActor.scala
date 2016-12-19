package system.crawler.host

import akka.actor.{Actor, PoisonPill}
import akka.http.scaladsl.model.Uri
import akka.pattern.pipe
import system.crawler.host.utils.{HttpLoader, UrlExtractor}

class PageLoaderActor extends Actor {
  private implicit val executionContext = context.dispatcher


  def receive(): Actor.Receive = {
    case PageLoaderActor.DownloadPages(url) =>
      handlePage(url)
  }


  private def handlePage(sourceUrl: Uri) = {
    def toResult(htmlBody: String) = {
      val extractedData = (new UrlExtractor).extractData(htmlBody, sourceUrl)
      PageLoaderActor.HtmlPageDataResult(extractedData._1, extractedData._2)
    }

    HttpLoader.load(sourceUrl).map(toResult) pipeTo sender() andThen {
      // Do it and die approach.
      case _ => self ! PoisonPill
    }
  }
}

object PageLoaderActor {

  case class DownloadPages(url: Uri)

  case class HtmlPageDataResult(htmlBody: String, extractedUrl: Set[Uri])

}
