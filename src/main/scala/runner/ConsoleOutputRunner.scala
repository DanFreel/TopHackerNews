package runner

import hnprovider.HackerNewsProvider
import hnprovider.datasource.FirebaseSource
import processor.{TopStoriesProcessor, TopStoryWithCommenters}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ConsoleOutputRunner extends App {
  private def printCmd(tsc: TopStoryWithCommenters) = {
    val topUserDetails =
      tsc
        .topCommenters
        .foldLeft("")((s, tc) => s + s" ${tc.userName} (${tc.commentsOnStory} for story - ${tc.totalComments} total) |")

    println(s"| ${tsc.title} | $topUserDetails")
  }

  val topStoriesWithCommentersFut: Future[Seq[TopStoryWithCommenters]] =
    new TopStoriesProcessor(HackerNewsProvider(new FirebaseSource)).getTopStoriesWithCommenters(30, 10)

  val topStoriesWithCommenters = Await.result(topStoriesWithCommentersFut, Duration.Inf)
  topStoriesWithCommenters.foreach(printCmd)
}
