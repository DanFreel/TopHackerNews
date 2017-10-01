package hnprovider.provider

import hnprovider.datasource.Source

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TopStoriesProvider(source: Source) {

  def loadTopStoryIdsFuture(numOfTopStories: Int): Future[Seq[Long]] =
    Future {
      loadTopStoryIds(numOfTopStories)
    }

  private def loadTopStoryIds(numOfTopStories: Int): Seq[Long] = {
    validateInput(numOfTopStories)
    source.loadTopNStoryIds(numOfTopStories)
  }

  private def validateInput(numOfTopStories: Int): Unit = {
    /*
      Notes to the reviewer:
      1) Under normal circumstances, I would use a logger library instead of using println.
      2) In this case here I provide a warning since a negative input won't terminate the application.
      It will just return an empty List. However, the warning is designed to help debug in case you wonder
      why empty List was returned.
     */
    if(numOfTopStories < 0)
      println(s"Warning - negative number of top stories.")
  }
}
