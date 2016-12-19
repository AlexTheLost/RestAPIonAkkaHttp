package system.crawler.host.utils

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

object HttpLoader {
  private implicit val actorSystem = ActorSystem("http")
  private implicit val actorMaterializer = ActorMaterializer()
  private implicit val executorContext = actorSystem.dispatcher

  private val requestIdentifier = new AtomicInteger(Int.MinValue)
  private val superPool = Http().superPool[Int]()


  //TODO: Have problem, host can redirect request, with status 301 or similar.
  def load(uri: Uri): Future[String] = Source.single(HttpRequest(uri = uri) -> requestIdentifier.getAndIncrement())
    .via(superPool)
    .runWith(Sink.head)
    .map(_._1)
    .map(_.get)
    .flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) => entity.dataBytes.runFold(ByteString(""))(_ ++ _).map(_.utf8String)
      case response@HttpResponse(code, _, _, _) => response.discardEntityBytes(); throw new IllegalStateException(s"Can't read from url: $uri, cause: $code")
    }
}
