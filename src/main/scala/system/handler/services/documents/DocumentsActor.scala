package system.handler.services.documents

import akka.actor.{Actor, PoisonPill, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import system.handler.domain.documents.Document

import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class DocumentsActor extends Actor {
  private implicit val executionContext = context.dispatcher

  def receive(): Actor.Receive = {
    case FindDocument(name) => findDocument(name)
    case AddDocument(document) => addDocument(document)
    case DelDocument(name) => delDocument(name)
    case CalculateAllWordCount => calculateAllWordCount()
    case DocumentCount => documentCount()
  }

  private def documentCount() = sender ! context.children.size

  private def calculateAllWordCount() = {
    def secondToFirst(destination: MutableMap[String, Int], source: Map[String, Int]) = {
      source.foreach(pair => {
        val key = pair._1
        val value = pair._2
        destination.get(key).fold(destination.put(key, value))(current => destination.put(key, value + current))
      })
      destination
    }

    implicit val timeout = Timeout(2 minute)

    val answer = Future.sequence(context.children.map(_.ask(CalculateWordCount).mapTo[Map[String, Int]]))
    val result = answer.map(_.foldLeft(MutableMap[String, Int]())(secondToFirst)).map(_.toMap)

    pipe(result) to sender
  }


  private def findDocument(name: String) = {
    context.child(name).fold({
      sender ! None
    })(_.forward(GetDocument))
  }

  private def addDocument(document: Document) = {
    def createDocument(document: Document) = {
      val name: String = document.name
      val docHolderActor = context.actorOf(Props[DocumentHolderActor], name)
      docHolderActor ! document
    }

    context.child(document.name).fold({
      createDocument(document)
      sender ! AddDocumentSuccess()
    })(_ => sender ! AddDocumentProblem(s"Document with name ${document.name} already exist."))
  }

  private def delDocument(name: String) = {
    context.child(name).fold(sender ! false)({ docHolder =>
      docHolder ! PoisonPill
      sender ! true
    })
  }
}

trait DocumentsRequest

case class AddDocument(document: Document) extends DocumentsRequest

case class FindDocument(name: String) extends DocumentsRequest

case class DelDocument(name: String) extends DocumentsRequest

case object CalculateAllWordCount extends DocumentsRequest

case object DocumentCount extends DocumentsRequest

trait DocumentsResponse

trait Add extends DocumentsResponse

case class AddDocumentSuccess() extends Add

case class AddDocumentProblem(cause: String) extends Add




