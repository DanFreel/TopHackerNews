package hnprovider.provider

import hnprovider.datasource.dataextractor.Extractor
import hnprovider.datasource.Source
import hnprovider.datasource.datakeys.Keys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CommentDetailsProvider(source: Source) {
  import CommentDetailsProvider._

  private val dataExtractor: Extractor = source.dataExtractor
  private val dataKeys: Keys = source.dataKeys

  def loadCommentDetailsFuture(commentId: Long): Future[Seq[CommentDetails]] = {
    val fut = Future {
      loadCommentDetails(commentId)
    }

    fut flatMap identity
  }

  private def loadCommentDetails(commentId: Long): Future[Seq[CommentDetails]] = {
    val commentDetailsWithSubCommentIds = extractCommentDetails(source.loadItem(commentId), commentId)
    val subCommentDetails = extractSubCommentDetails(commentDetailsWithSubCommentIds)

    subCommentDetails map (_ ++ commentDetailsWithSubCommentIds.map(_.commentDetails))
  }

  private def extractSubCommentDetails(commentDetailsWithSubCommentIds: Option[CommentDetailsWithSubCommentIds])
  : Future[Seq[CommentDetails]] = {
    val subCommentDetails: Seq[Future[Seq[CommentDetails]]] = for(
      comDetWithSubComIds <- commentDetailsWithSubCommentIds.toSeq;
      subCommentId <- comDetWithSubComIds.subCommentIds) yield loadCommentDetailsFuture(subCommentId.toLong)

    Future.sequence(subCommentDetails).map(_.flatten)
  }

  private def extractCommentDetails(commentMap: Map[String, Any], commentId: Long)
  : Option[CommentDetailsWithSubCommentIds] = {
    def isDeleted: Boolean = dataExtractor.extractBoolean(commentMap.getOrElse(dataKeys.isDeletedKey, false))

    def extractUserName: String =
      dataExtractor.extractString(commentMap.getOrElse(dataKeys.userNameKey, defaultUserName.format(commentId)))

    def extractCommentIds: Seq[Long] = dataExtractor.extractIds(commentMap.getOrElse(dataKeys.commentIdsKey, Seq.empty))

    if (!isDeleted)
      Some(
        CommentDetailsWithSubCommentIds(
          CommentDetails(extractUserName),
          extractCommentIds))
    else
      None
  }
}

object CommentDetailsProvider {
  val defaultUserName = "Default User Name[%d]"
}

case class CommentDetails(userName: String)
case class CommentDetailsWithSubCommentIds(commentDetails: CommentDetails, subCommentIds: Seq[Long])
