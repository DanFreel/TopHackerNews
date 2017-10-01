package hnprovider

import hnprovider.datasource.Source
import hnprovider.provider.{StoryDetails, StoryDetailsProvider, TopStoriesProvider}

import scala.concurrent.Future

class HackerNewsProvider(topStoriesProv: TopStoriesProvider,
                         storyDetailsProv: StoryDetailsProvider) {

  def loadTopStoryIds(numOfTopStories: Int): Future[Seq[Long]] = topStoriesProv.loadTopStoryIdsFuture(numOfTopStories)
  def loadStoryDetails(storyId: Long): Future[StoryDetails] = storyDetailsProv.loadStoryDetailsFuture(storyId)
}

object HackerNewsProvider {
  def apply(source: Source): HackerNewsProvider =
    new HackerNewsProvider(
      new TopStoriesProvider(source),
      StoryDetailsProvider(source))
}
