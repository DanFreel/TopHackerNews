package hnprovider.datasource

import hnprovider.datasource.dataextractor.Extractor
import hnprovider.datasource.datakeys.Keys

trait Source {
  val unexpectedTypeErrorMsg = "Unexpected type return from JSON.parseFull: %s"

  def dataExtractor: Extractor
  def dataKeys: Keys

  def loadTopNStoryIds(numTopStories: Int): Seq[Long]
  def loadItem(itemId: Long): Map[String, Any]

  protected def loadSeqData(path: String): Seq[Any] = loadData[Seq[Any]](path)(() => Seq.empty)
  protected def loadMapData(path: String): Map[String, Any] = loadData[Map[String, Any]](path)(() => Map.empty)
  protected def loadData[Col <: Iterable[Any]](path: String)(emptyFunc: () => Col): Col
}
