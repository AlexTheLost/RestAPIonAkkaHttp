package system.handler.services.documents

import akka.actor.Actor
import system.handler.domain.documents.Document
import system.handler.services.documents.utils.text.{WordStatisticsCalculation, WordStatisticsPrepare}

class DocumentHolderActor extends Actor {
  var document: Document = _

  def receive(): Actor.Receive = {
    case doc: Document =>
      document = doc
    case GetDocument =>
      sender() ! Option(document)
    case CalculateWordCount =>
      sender() ! WordStatisticsCalculation.counting(WordStatisticsPrepare.tokenizeAndNormalize(document.body))
  }
}


trait DocumentHolderRequest

case object GetDocument extends DocumentHolderRequest

case object CalculateWordCount extends DocumentHolderRequest

trait DocumentHolderResponse
