package unitl.system.actors.services.documents.utils.text

import org.scalatest.{FlatSpec, Matchers}
import system.handler.services.documents.utils.text.{WordStatisticsCalculation, WordStatisticsPrepare}


class WordStatisticsPrepareTest extends FlatSpec with Matchers {
  "Poem" should "separate by words" in {
    val poem =
      """
        |LITHUANIA, my country, thou art like health ;
        |how much thou shouldst be prized only he can learn who has lost thee.
        |To-day thy beauty in all its splendour I see and describe, for I yearn for thee.
      """.stripMargin


    val actual = WordStatisticsPrepare.tokenizeAndNormalize(poem)

    val expected = Seq(
      "lithuania", "my", "country", "thou", "art", "like", "health",
      "how", "much", "thou", "shouldst", "be", "prized", "only", "he", "can", "learn", "who", "has", "lost", "thee",
      "to", "day", "thy", "beauty", "in", "all", "its", "splendour", "i", "see", "and", "describe", "for", "i", "yearn", "for", "thee"
    )

    actual should equal(expected)
  }
}


class WordStatisticsCalculationTest extends FlatSpec with Matchers {
  "A word count in sequence" should "be equal to the specified" in {
    val poem = Seq(
      "lithuania", "my", "country", "thou", "art", "like", "health",
      "how", "much", "thou", "shouldst", "be", "prized", "only", "he", "can", "learn", "who", "has", "lost", "thee",
      "to", "day", "thy", "beauty", "in", "all", "its", "splendour", "i", "see", "and", "describe", "for", "i", "yearn", "for", "thee"
    )

    val expected = Map(
      "lithuania" -> 1, "my" -> 1, "country" -> 1, "thou" -> 2, "art" -> 1, "like" -> 1,
      "health" -> 1, "how" -> 1, "much" -> 1, "shouldst" -> 1, "be" -> 1, "prized" -> 1,
      "only" -> 1, "he" -> 1, "can" -> 1, "learn" -> 1, "who" -> 1, "has" -> 1,
      "lost" -> 1, "thee" -> 2, "to" -> 1, "day" -> 1, "thy" -> 1, "beauty" -> 1,
      "in" -> 1, "all" -> 1, "its" -> 1, "splendour" -> 1, "i" -> 2, "see" -> 1,
      "and" -> 1, "describe" -> 1, "for" -> 2, "yearn" -> 1
    )

    val actual = WordStatisticsCalculation.counting(poem)

    actual should equal(expected)
  }
}

