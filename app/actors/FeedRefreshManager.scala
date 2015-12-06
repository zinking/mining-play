package actors

import akka.actor.{Actor, Props}
import mining.io.dao.FeedDao

/**
 * Created by awang on 30/8/15.
 */
object FeedRefreshManager {
  def props = Props[FeedRefreshManager]

  case class RefreshAllFeeds()
}

class FeedRefreshManager extends Actor {
  import FeedRefreshManager._
  val feedDAO = FeedDao()

  def receive = {
    case RefreshAllFeeds() =>
        feedDAO.getAllFeeds.foreach( feed=>
            feedDAO.createOrUpdateFeed( feed.url )
        )
  }
}