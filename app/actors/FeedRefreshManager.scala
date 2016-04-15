package actors

import java.util.Date

import akka.actor.{ActorLogging, Actor, Props}
import com.typesafe.config.{ConfigFactory, Config}
import mining.io.Feed
import mining.io.dao.FeedDao
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Feed refresh mananger
 *
 * Created by awang on 30/8/15.
 */

case class RefreshFirstQuarterFeeds()
case class RefreshSecondQuarterFeeds()
case class RefreshThirdQuarterFeeds()
case class RefreshAllFeeds()

object FeedRefreshManager {
    def props = Props[FeedRefreshManager]
    val conf:Config = ConfigFactory.load
    val venv = conf.getString("env")
    val env = Option(System.getenv("env")).getOrElse(venv)
    val feedChgInt = conf.getConfig(env).getInt("feedchg-int")
    val refreshInt = conf.getConfig(env).getInt("refresh-int")
    
    val d1 = feedChgInt
    val d2 = feedChgInt*4
    val d3 = feedChgInt*16
    val d4 = feedChgInt*64

    val refreshFirstBatch = RefreshFirstQuarterFeeds()
    val refreshSecondBatch = RefreshSecondQuarterFeeds()
    val refreshThirdBatch = RefreshThirdQuarterFeeds()
    val refreshAllMsg = RefreshAllFeeds()
}

class FeedRefreshManager extends Actor with ActorLogging {
    import FeedRefreshManager._ 
    val feedDAO = FeedDao()
    context.system.scheduler.schedule(d1 seconds, d1 seconds, self, refreshFirstBatch)
    context.system.scheduler.schedule(d2 seconds, d2 seconds, self, refreshSecondBatch)
    context.system.scheduler.schedule(d3 seconds, d3 seconds, self, refreshThirdBatch)
    context.system.scheduler.schedule(d4 seconds, d4 seconds, self, refreshAllMsg)


    def getQuarterFeeds( index:Int, duration:Int):Iterable[Feed] = {
        val oFeeds = feedDAO.getAllFeeds.toList.sortBy{f=>
            (-f.refreshItemCount, f.avgRefreshDuration, f.errorCount)
        }

        val nToTake:Int = (oFeeds.size*(index/4.0)).toInt
        oFeeds.take(nToTake).filter{ feed =>
            val duration = System.currentTimeMillis() - feed.checked.getTime
            val errorRate = feed.errorCount / feed.visitCount.asInstanceOf[Double]
            if (duration > duration*1000){
                log.info("skip {} as just checked", feed.xmlUrl)
                false
            } else if (feed.errorCount>10 && errorRate>0.75){
                log.info("skip {} as just failing too much", feed.xmlUrl)
                false
            } else {
                true
            }
        }
    }


    def receive = {
        case msg:RefreshFirstQuarterFeeds =>
            getQuarterFeeds(1,d1).foreach({feed=>
                log.info("Refresh feed {}",feed.xmlUrl)
                feedDAO.createOrUpdateFeed(feed.xmlUrl)
            })

        case msg:RefreshSecondQuarterFeeds =>
            getQuarterFeeds(2,d2).foreach({feed=>
                log.info("Refresh feed {}",feed.xmlUrl)
                feedDAO.createOrUpdateFeed(feed.xmlUrl)
            })

        case msg:RefreshThirdQuarterFeeds =>
            getQuarterFeeds(3,d3).foreach({feed=>
                log.info("Refresh feed {}",feed.xmlUrl)
                feedDAO.createOrUpdateFeed(feed.xmlUrl)
            })

        case msg:RefreshAllFeeds =>
            getQuarterFeeds(4,d4).foreach({feed=>
                log.info("Refresh feed {}",feed.xmlUrl)
                feedDAO.createOrUpdateFeed(feed.xmlUrl)
            })
    }
}