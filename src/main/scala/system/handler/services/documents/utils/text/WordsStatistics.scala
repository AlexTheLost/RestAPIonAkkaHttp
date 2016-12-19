package system.handler.services.documents.utils.text


object WordStatisticsCalculation {
  def counting(words: Seq[String]): Map[String, Int] = words.groupBy(identity).mapValues(_.size)
}

object WordStatisticsPrepare {
  def tokenizeAndNormalize(text: String): Seq[String] = {
    text.split("\\W").filter(!_.isEmpty).map(_.toLowerCase)
  }
}
