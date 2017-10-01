package hnprovider.provider

import hnprovider.datasource.dataextractor.Extractor
import hnprovider.datasource.Source
import hnprovider.datasource.datakeys.Keys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class StoryDetailsProvider(source: Source, commentProvider: CommentDetailsProvider) {
  import StoryDetailsProvider._

  private val dataExtractor: Extractor = source.dataExtractor
  private val dataKeys: Keys = source.dataKeys

  def loadStoryDetailsFuture(storyId: Long): Future[StoryDetails] =
    Future {
      loadStoryDetails(storyId)
    }

  private def loadStoryDetails(storyId: Long): StoryDetails = extractStoryDetails(source.loadItem(storyId), storyId)

  private def extractStoryDetails(storyMap: Map[String, Any], storyId: Long): StoryDetails = {
    def extractTitle: String =
      dataExtractor.extractString(storyMap.getOrElse(dataKeys.titleKey, defaultTitle.format(storyId)))

    def extractCommentDetails: Seq[CommentDetails] =
      dataExtractor
        .extractIds(storyMap.getOrElse(dataKeys.commentIdsKey, Seq.empty))
        .flatMap { id =>
          val commentDetailsFuture = commentProvider.loadCommentDetailsFuture(id)
          Await.result(commentDetailsFuture, Duration.Inf)
        }

    StoryDetails(extractTitle, extractCommentDetails)
  }
}

object StoryDetailsProvider {
  val defaultTitle = "Default Title[%d]"

  def apply (source: Source): StoryDetailsProvider =
    new StoryDetailsProvider(
      source,
      new CommentDetailsProvider(source))
}

case class StoryDetails(title: String, commentDetails: Seq[CommentDetails])
