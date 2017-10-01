package processor

import hnprovider.HackerNewsProvider
import hnprovider.provider.StoryDetails

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TopStoriesProcessor(hnProvider: HackerNewsProvider) {
  def getTopStoriesWithCommenters(numTopStories: Int, numTopCommenters: Int): Future[Seq[TopStoryWithCommenters]] = {
    val storyIdsFut: Future[Seq[Long]] = hnProvider.loadTopStoryIds(numTopStories)
    val storyDetailsFut: Future[Seq[StoryDetails]] =
      storyIdsFut.flatMap(stories =>
        Future.sequence(stories.map(hnProvider.loadStoryDetails)))

    val totalsByUserFut: Future[Map[String, Int]] = storyDetailsFut map getTotalsByCommenter

    for(
      storyDetails <- storyDetailsFut;
      totalsByUser <- totalsByUserFut) yield makeTopStoriesWithCommenters(numTopCommenters, storyDetails, totalsByUser)
  }

  private def getTotalsByCommenter(storyDetails: Seq[StoryDetails]): Map[String, Int] = {
    def incrementUserCommentCount(userName: String, currentCommentCoundMap: Map[String, Int]): (String, Int) =
      userName -> (currentCommentCoundMap.getOrElse(userName, 0) + 1)

    storyDetails
      .flatMap(_.commentDetails)
      .foldLeft[Map[String, Int]](Map.empty)((m, cd) => m + incrementUserCommentCount(cd.userName, m))
  }

  private def makeTopStoriesWithCommenters(numTopCommenters: Int,
                                           storyDetails: Seq[StoryDetails],
                                           totalsByUser: Map[String, Int]): Seq[TopStoryWithCommenters] = {
    def topCommenters(sd: StoryDetails): Seq[TopCommenter] = {
      getTotalsByCommenter(Seq(sd))
        .toSeq
        .sortBy{case (_, commentsCount) => -commentsCount}
        .slice(0, numTopCommenters)
        .map{case (name, freq) => TopCommenter(name, freq, totalsByUser(name))}
    }

    storyDetails.map(sd => TopStoryWithCommenters(sd.title, topCommenters(sd)))
  }
}

case class TopStoryWithCommenters(title: String, topCommenters: Seq[TopCommenter])
case class TopCommenter(userName: String, commentsOnStory: Int, totalComments: Int)
