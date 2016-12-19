package system.crawler.host

import java.util.UUID

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, OneForOneStrategy, PoisonPill, Props, Status, SupervisorStrategy}
import akka.http.scaladsl.model.Uri
import system.crawler.CrawlerActor
import system.meta.logger.AppAkkaLogger

import scala.collection.mutable.{Set => MutableSet}
import scala.concurrent.duration._

class HostLoaderActor extends Actor {
  private implicit val actorSystem = context.system

  private val downloadedUrls = MutableSet[Uri]()
  private var remainingLinkToDownload = 0


  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1.second) {
    case e: Throwable => Stop
  }

  def receive(): Actor.Receive = {
    case HostLoaderActor.DownloadHostPages(url) => handleUrl(url)
    case PageLoaderActor.HtmlPageDataResult(body, extractedUrl) => consumeResultOfHandlingPage(body, extractedUrl)
    case Status.Failure(e) => handleDownloadFailure(e)
  }


  private def consumeResultOfHandlingPage(body: String, extractUrls: Set[Uri]) = {
    context.parent ! CrawlerActor.NewHtmlDocument(s"html-page-${UUID.randomUUID().toString}", body)
    extractUrls.diff(downloadedUrls).foreach(handleUrl)
    decrementRemainingLinkCount()
  }

  private def handleUrl(url: Uri) = {
    context.actorOf(Props[PageLoaderActor]) ! PageLoaderActor.DownloadPages(url)
    downloadedUrls.add(url)
    remainingLinkToDownload += 1
  }

  private def handleDownloadFailure(exception: Throwable) = {
    AppAkkaLogger("HostLoaderActor").error(exception, s"Can't download url.")
    decrementRemainingLinkCount()
  }

  private def decrementRemainingLinkCount() = {
    remainingLinkToDownload -= 1
    if (remainingLinkToDownload == 0)
      self ! PoisonPill
  }
}

object HostLoaderActor {

  case class DownloadHostPages(url: Uri)

}
