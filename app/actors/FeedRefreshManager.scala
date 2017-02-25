package actors

import java.util.Date
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorLogging, Actor, Props}
import com.typesafe.config.{ConfigFactory, Config}
import mining.io.Feed
import mining.io.dao.FeedDao
import scala.collection.concurrent.Map
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.collection.JavaConverters._
import scala.language.reflectiveCalls

/**
 * Feed refresh manager
 *
 * Created by awang on 30/8/15.
 */


case class RefreshFeeds(index:Int, duration:Int)

object FeedRefreshManager {
    def props = Props[FeedRefreshManager]
    val conf:Config = ConfigFactory.load
    val venv = conf.getString("env")
    val env = Option(System.getenv("env")).getOrElse(venv)
    val feedChgInt = conf.getConfig(env).getInt("feedchg-int")
    val refreshInt = conf.getConfig(env).getInt("refresh-int")
    
    val d1 = refreshInt
    val d2 = d1*4
    val d3 = d1*16
    val d4 = d1*64

    val refresh1 = RefreshFeeds(1, d1)
    val refresh2 = RefreshFeeds(2, d2)
    val refresh3 = RefreshFeeds(3, d3)
    val refresh4 = RefreshFeeds(4, d4)

    val CleanLocks = "CleanLocks"
}

class FeedRefreshManager extends Actor with ActorLogging {
    import FeedRefreshManager._ 
    val feedDAO = FeedDao()
    context.system.scheduler.schedule(d1 seconds, d1 seconds, self, refresh1)
    context.system.scheduler.schedule(d2 seconds, d2 seconds, self, refresh2)
    context.system.scheduler.schedule(d3 seconds, d3 seconds, self, refresh3)
    context.system.scheduler.schedule(d4 seconds, d4 seconds, self, refresh4)
    context.system.scheduler.schedule(1 days, 1 days, self,  CleanLocks)

    val feedLock: Map[String, String] = new ConcurrentHashMap[String,String]().asScala


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
        case CleanLocks =>
            feedLock.clear()
            log.info("Cleaned up all the locks")

        case RefreshFeeds(id, duration) =>
            val banner = "***********************************************************************"
            log.info(" BEGIN {} {} {}", id, duration, banner)
            getQuarterFeeds(id,duration).foreach({feed=>
                val feedUrl = feed.xmlUrl
                feedLock.get(feedUrl) match {
                    case Some(xml) =>
                        log.info("Skip locked feed quarter {} duration {} url {}", id, duration, feedUrl)
                    case None =>
                        feedLock.put(feedUrl, "1")
                        log.info("Refresh feed quarter {} duration {} url {}", id, duration, feedUrl)
                        feedDAO.createOrUpdateFeed(feed.xmlUrl) onComplete {
                            case _ => feedLock.remove(feedUrl)
                        }
                }

            })

            log.info(" ENDET {} {} {}", id, duration, banner)
    }
}