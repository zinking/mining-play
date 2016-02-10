package actors

import java.util.Date

import akka.actor.{ActorLogging, Actor, Props}
import com.typesafe.config.{ConfigFactory, Config}
import mining.io.dao.FeedDao
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

/**
 * Feed refresh mananger
 *
 * Created by awang on 30/8/15.
 */

case class RefreshAllFeeds()

object FeedRefreshManager {
    def props = Props[FeedRefreshManager]
    val conf:Config = ConfigFactory.load
    val venv = conf.getString("env")
    val env = Option(System.getenv("env")).getOrElse(venv)
    val feedChgInt = conf.getConfig(env).getInt("feedchg-int")
    val refreshInt = conf.getConfig(env).getInt("refresh-int")

    val refreshAllMsg = RefreshAllFeeds()
}

class FeedRefreshManager extends Actor with ActorLogging {
    val feedDAO = FeedDao()
    context.system.scheduler.schedule(
        FeedRefreshManager.refreshInt seconds, FeedRefreshManager.refreshInt seconds, self, FeedRefreshManager.refreshAllMsg)


    def receive = {
        case msg:RefreshAllFeeds =>
            val now = new Date
            feedDAO.getAllFeeds.foreach{feed=>
                val duration = System.currentTimeMillis() - feed.checked.getTime
                if (duration>FeedRefreshManager.feedChgInt*1000){
                    log.info("Refresh feed {}",feed.xmlUrl)
                    try{
                        feedDAO.createOrUpdateFeed(feed.xmlUrl)
                    } catch {
                        case e:Throwable =>
                            log.error(e.getMessage,e)
                    }

                }
            }
    }
}