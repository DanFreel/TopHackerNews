package hnprovider.datasource

import hnprovider.datasource.dataextractor.FirebaseExtractor
import hnprovider.datasource.datakeys.FirebaseKeys

import scala.util.parsing.json.JSON

class FirebaseSource extends Source {
  import FirebaseSource._

  /** Note to Reviewer: JSON.parseFull treats all numbers as decimals, so it returns a double.
  * This is a bit troublesome, especially when we pass the ids around into URLs.
  * Either I can clean the Ids I pull or change the number parser as I did here.
  * This is definitely not ideal and I would consider using a different JSON parser.
  */
  JSON.globalNumberParser = (input: String) => try input.toLong catch { case _: NumberFormatException => input.toDouble}

  val dataExtractor = new FirebaseExtractor
  val dataKeys = new FirebaseKeys

  def loadTopNStoryIds(numTopStories: Int): Seq[Long] =
    dataExtractor.extractIds(
      loadSeqData(topStoriesPath).slice(0, numTopStories))

  def loadItem(itemId: Long): Map[String, Any] = loadMapData(itemPathTemplate.format(itemId))

  def loadData[Col <: Iterable[Any]](path: String)(emptyFunc: () => Col): Col = {
    val data = io.Source.fromURL(pathPrefix + path)
    val result = data.mkString
    val json = JSON.parseFull(result)

    json match {
      case Some(c: Col) => c
      case Some(unexpectedType) =>
        throw new IllegalArgumentException(s"Unexpected type return from JSON.parseFull: $unexpectedType")
      case None => emptyFunc()
    }
  }
}

object FirebaseSource {
  val pathPrefix = "https://hacker-news.firebaseio.com/v0/"
  val topStoriesPath = "topstories.json"
  val itemPathTemplate = "item/%d.json"
}
