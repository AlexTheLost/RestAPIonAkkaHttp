package system.handler.api.http

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Created, NotFound, OK}
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.server.directives.MarshallingDirectives.{as, entity}
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.{path, pathEndOrSingleSlash, pathPrefix}
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol
import system.crawler.CrawlerActor
import system.handler.domain.documents.Document
import system.handler.services.documents.{DocumentCount, DocumentsActor}
import system.meta.config.HttpConfig
import system.meta.logger.AppAkkaLogger

import scala.util.Try


object RestApi extends HttpConfig {

  def apply()(implicit actorSystem: ActorSystem): Unit = {
    implicit val executionContext = actorSystem.dispatcher
    implicit val actorMaterializer = ActorMaterializer()

    val route = new RestApi(actorSystem).route()
    val httpApi = Http().bindAndHandle(route, host, port)

    httpApi.onComplete(_.fold(
      exception => {
        AppAkkaLogger().error("Exception during start appplication", exception)
        throw exception
      },
      serverBinding => {
        AppAkkaLogger().info(s"Http Rest Api start on: ${
          serverBinding.localAddress
        }")
      }))
  }
}

private class RestApi(actorSystem: ActorSystem) extends DefaultJsonProtocol with SprayJsonSupport {

  import akka.http.scaladsl.server.PathMatcher._
  import akka.util.Timeout
  import system.handler.services.documents.{Add, AddDocument, AddDocumentProblem, AddDocumentSuccess, CalculateAllWordCount, DelDocument, FindDocument}

  import scala.concurrent.duration._
  import scala.language.postfixOps

  private implicit val executorContext = actorSystem.dispatcher
  private implicit val timeout = Timeout(5 seconds)
  // Start Document-Handler-System:
  private val documentsActor = actorSystem.actorOf(Props[DocumentsActor], "DocumentsActor")
  // Start Crawler-System:
  private val crawler = actorSystem.actorOf(Props(new CrawlerActor(documentsActor)), "crawler")

  def route(): Route = documentRoutes ~ documentStatisticRoute ~ crawlerRoutes


  case class CrawlerRequest(url: String)

  private def crawlerRoutes = pathPrefix("crawler") {
    implicit val crawlerRequestFormats = jsonFormat1(CrawlerRequest)
    path("download") {
      post {
        pathEndOrSingleSlash {
          entity(as[CrawlerRequest]) { crawlerRequest =>
            val urlAsString = crawlerRequest.url

            Try(Uri(crawlerRequest.url)).fold(
              _ => complete(BadRequest, s"Incorrect url form: $urlAsString"),
              url => {
                if (url.isAbsolute) {
                  crawler ! CrawlerActor.DownloadHostPages(urlAsString)
                  complete(OK)
                } else {
                  complete(BadRequest, s"Required a full url: $urlAsString")
                }
              })
          }
        }
      }
    }
  }

  private def documentRoutes = pathPrefix("document") {
    implicit val askSender = documentsActor
    implicit val DocumentFormats = jsonFormat2(Document)
    path(Segment) { name =>
      pathEndOrSingleSlash {
        get {
          onSuccess(documentsActor.ask(FindDocument(name)).mapTo[Option[Document]])(result =>
            result.fold(complete(NotFound))(complete(OK, _)))
        } ~
          delete {
            onSuccess(documentsActor.ask(DelDocument).mapTo[Boolean]) {
              case true => complete(OK)
              case false => complete(NotFound)
            }
          }
      }
    } ~
      pathEndOrSingleSlash {
        post {
          entity(as[Document]) { document =>
            def addDocument(document: Document) = documentsActor
              .ask(AddDocument(document))
              .mapTo[Add]

            onSuccess(addDocument(document)) {
              case AddDocumentSuccess() =>
                complete(Created)
              case AddDocumentProblem(cause) =>
                complete(BadRequest, s"Document not created, cause: $cause")
            }
          }
        }
      }
  }

  private def documentStatisticRoute = pathPrefix("doc-statistics") {
    pathPrefix("all") {
      pathPrefix("docs") {
        pathPrefix("counts") {
          pathEndOrSingleSlash {
            get {
              onSuccess(documentsActor.ask(DocumentCount).mapTo[Int]) { data =>
                complete(OK, data.toString)
              }
            }
          }
        }
      } ~
        pathPrefix("words") {
          pathPrefix("counts") {
            pathEndOrSingleSlash {
              get {
                onSuccess(documentsActor.ask(CalculateAllWordCount).mapTo[Map[String, Int]]) { data =>
                  complete(OK, data)
                }
              }
            }
          }
        }
    }
  }
}
