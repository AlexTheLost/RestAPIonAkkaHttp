package system.crawler

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import akka.http.scaladsl.model.Uri
import system.crawler.host.HostLoaderActor
import system.handler.domain.documents.Document
import system.handler.services.documents.AddDocument
import system.meta.logger.AppAkkaLogger

class CrawlerActor(private val documentHolder: ActorRef) extends Actor {
  private implicit val actorSystem = context.system


  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _: Throwable => Stop
  }

  def receive(): Actor.Receive = {
    case CrawlerActor.DownloadHostPages(url) => download(url)
    case CrawlerActor.NewHtmlDocument(name, document) => processNewDocument(name, document)

  }


  private def download(url: Uri) = context.actorOf(Props[HostLoaderActor]) ! HostLoaderActor.DownloadHostPages(url)

  private def processNewDocument(name: String, document: String) = {
    AppAkkaLogger().debug(s"New document: name=$name (length=${document.length})")
    documentHolder ! AddDocument(Document(name, document))
  }
}

object CrawlerActor {

  case class DownloadHostPages(url: Uri)

  case class NewHtmlDocument(name: String, document: String)

}
