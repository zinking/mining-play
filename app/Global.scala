import actors.FeedRefreshManager
import play.libs.Akka

import play.api._

/**
 * Created by awang on 2/1/16.
 */
object Global extends GlobalSettings {

    override def onStart(app: Application) {
        if (!FeedRefreshManager.env.equals("test")){
            Akka.system().actorOf(FeedRefreshManager.props, "feed-refresh-actor")
        }
        Logger.info("Application has started")
    }

    override def onStop(app: Application) {
        Logger.info("Application shutdown...")
    }

}
