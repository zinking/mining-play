package controllers

import java.util.Date

import mining.io._
import mining.io.dao.{UserDao, FeedDao}
import models.JsonUtils._
import org.apache.commons.io.FileUtils
import play.api.libs.json._
import play.api.mvc.Action
import play.api.Logger
import scala.concurrent.duration._

import scala.concurrent.Await

class UserController extends MiningController {
    val feedDAO = FeedDao()

    /**
     * get the story contents of specified story
     * input data params format: [{Feed:xmlUrl,Story:Id}]
     * @return the story content 
     *         result format: [""]
     */
    def getContents = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val jcontents = param.as[Seq[JsObject]]

        val storyContents = jcontents.map(ri => {
            val storyId = (ri \ "StoryId").as[Long]
            val story = feedDAO.getStoryById(storyId)
            Json.obj(
                "Summary" -> JsString(story.description),
                "Content" -> JsString(story.content)
            )
        })
        Logger.info(s"USER[${user.userId}] getContents ${jcontents.size} stories ")
        Ok(Json.toJson(storyContents)).as("application/json")
    }

    /**
     * get the stories of the feed
     * input data params format: {C:pageNo,F:xmlUrl}
     * @return feed stories
     *         result format: { Cursor:pageNo, Stories:[Story], Stars:[htmlLink]}
     */
    def getFeedStories = AuthAction { (user,request)  =>
        //get stories of a feed, with cursor/offset
        val uid = user.userId
        val param = request.body.asJson.get
        val c = (param \ "C").as[Int]
        val f = (param \ "F").as[String]
        val stories = feedDAO.getFeedStories(f, pageNo = c)
        val storyIds:List[Long] = stories.map(_.id).toList
        val userReadStoryIds = userDAO.getUserReadStories(uid, storyIds)
        val userStarStoryIds = userDAO.getUserStarStories(uid, storyIds)
        val feedContent = Json.obj(
            "Cursor" -> JsNumber(c),
            "Stories" -> Json.toJson(stories.map(Json.toJson(_))),
            "UserReadStoryIds" -> Json.toJson(userReadStoryIds),
            "UserStarStoryIds" -> Json.toJson(userStarStoryIds))

        Logger.info(s"USER[${user.userId}] getFeedStories ${stories.size} stories of page $c ")
        Ok(feedContent).as("application/json")
    }

    /**
     * get the stories of the feed
     * input data params format: {C:pageNo,F:xmlUrl}
     * @return feed stories
     *         result format: { Cursor:pageNo, Stories:[Story], Stars:[htmlLink]}
     */
    def getFeedsStories = AuthAction { (user,request)  =>
        //get stories of a feed, with cursor/offset
        val uid = user.userId
        val param = request.body.asJson.get
        val c = (param \ "C").as[Int]
        val fs = (param \ "FS").as[List[String]]
        val stories:List[Story] = fs.flatMap{ xmlUrl =>
            feedDAO.getFeedStories(xmlUrl, pageNo = c)
        }
        val storyIds:List[Long] = stories.map(_.id)
        val userReadStoryIds = userDAO.getUserReadStories(uid, storyIds)
        val userStarStoryIds = userDAO.getUserStarStories(uid, storyIds)
        val feedContent = Json.obj(
            "Cursor" -> JsNumber(c),
            "Stories" -> Json.toJson(stories.map(Json.toJson(_))),
            "UserReadStoryIds" -> Json.toJson(userReadStoryIds),
            "UserStarStoryIds" -> Json.toJson(userStarStoryIds))

        Logger.info(s"USER[${user.userId}] getFeedsStories ${stories.size} stories of page $c ")
        Ok(feedContent).as("application/json")
    }

    def getMineUserFromUrl( xmlUrl:String): Option[User] = {
        val pat=raw"http\:\/\/readmine.co\/users\?email=(.*)".r

        if (xmlUrl.startsWith(UserDao.IdPrefix)) {
            pat findFirstMatchIn xmlUrl flatMap{ mat =>
                val email = mat.group(1)
                userDAO.getUserByEmail(email).flatMap{ user=>
                    Some(user)
                }
            }
        } else{
            None
        }
    }

    /**
     * get user followed star stories
     * input data params format: {C:pageNo,F:xmlUrl}
     * @return feed stories
     *         result format: { Cursor:pageNo, Stories:[Story], Stars:[htmlLink]}
     */
    def getFollowStarStories = AuthAction { (user,request)  =>
        //get stories of a feed, with cursor/offset
        val param = request.body.asJson.get
        val c = (param \ "C").as[Int]
        val fs = (param \ "FS").as[List[String]]

        val stories:List[Story] = fs.flatMap{ xmlUrl =>
            val stories:Option[Iterable[Story]] = getMineUserFromUrl(xmlUrl) flatMap { user =>
                val starStories = userDAO.getUserStarStories(user.userId, pageNo = c)
                Some(starStories.map{ story=>
                    story.copy(feedId = -user.userId, id = -story.id)
                })
            }
            stories.getOrElse(List.empty)
        }
        val storyIds:List[Long] = stories.map(_.id)
        val userReadStoryIds = storyIds //starred stories are all read
        val userStarStoryIds = storyIds
        val feedContent = Json.obj(
            "Cursor" -> JsNumber(c),
            "Stories" -> Json.toJson(stories.map(Json.toJson(_))),
            "UserReadStoryIds" -> Json.toJson(userReadStoryIds),
            "UserStarStoryIds" -> Json.toJson(userStarStoryIds))

        Logger.info(s"USER[${user.userId}] getFollowStarStories ${stories.size} stories of page $c ")
        Ok(feedContent).as("application/json")
    }

    /**
     * list user feeds and stories at homepage
     * @return user Opml/Stories/Feeds/Stars/
     */
    def listFeeds = AuthAction { (user,request)  =>
        val uid = user.userId
        val opml: Opml = userDAO.getUserOpml(uid).getOrElse(new Opml(uid, Nil))
        val opmllist = Json.toJson(opml)
        val feeds = opml.getAllOutlines
        val feedIds = feedDAO.getOpmlFeeds(opml)
        val feedStats = userDAO.getUserFeedUnreadSummary(uid)
        val indexContent = Json.obj(
            "Opml" -> Json.toJson(opmllist),
            "Feeds" -> Json.toJson(feeds.map(Json.toJson(_))),
            "FeedIds" -> Json.toJson(feedIds.map(Json.toJson(_))), //TODO: refactor, user subscription should from feed object itself, not opml
            "UserReadFeedStats" -> Json.toJson(feedStats.map(Json.toJson(_)))
        )

        Logger.info(s"USER[${user.userId}] listFeeds ${feeds.size} feeds ")
        Ok(indexContent).as("application/json")
    }


    /**
     * list user shared stories
     * @return user Opml/Stories/Feeds/Stars/ -- faked
     */
    def listStarFeeds = AuthAction { (user,request)  =>
        val uid = user.userId
        val followUsers = userDAO.getFollowingUsers(uid)
        val followOutlines = followUsers.map{ user =>
            val title = user.email
            val xmlUrl = s"http://readmine.co/users?email=${user.email}"
            OpmlOutline(title, xmlUrl, "USER", title, xmlUrl )
        }
        val opml: Opml = Opml(-user.userId, followOutlines)
        val opmllist = Json.toJson(opml)
        val n = followUsers.size

        val feedIds: Iterable[Feed] = (0 to n-1).map{i=>
            val user = followUsers(i)
            val outline = followOutlines(i)
            val feed:Feed = FeedFactory.outline2Feed(outline)
            feed.copy(feedId = -user.userId)
        }
        val feeds = opml.getAllOutlines


        val indexContent = Json.obj(
            "StarOpml" -> Json.toJson(opmllist),
            "Feeds" -> Json.toJson(feeds.map(Json.toJson(_))),
            "FeedIds" -> Json.toJson(feedIds.map(Json.toJson(_))) //TODO: refactor, user subscription should from feed object itself, not opml
        )

        Logger.info(s"USER[${user.userId}] list ${feeds.size} feeds ")
        Ok(indexContent).as("application/json")
    }

    /**
     * mark user read specified story
     * input data param format: [{StoryId:0,FeedId:0}]
     * @return 1 if success
     */
    def markRead = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val slist = param.as[List[JsObject]]
        slist.foreach(item => {
            val storyId = (item \ "StoryId").as[Long]
            val feedId = (item \ "FeedId").as[Long]
            val read = (item \ "Read").as[Int]
            //userDAO.updateUserStoryRead(uid,feedId,storyId,read)
            userDAO.setUserReadStat(uid,feedId,storyId,read)
        })
        Logger.info(s"USER[${user.userId}] markRead $slist ")
        Ok("1").as("application/json")
    }

    /**
     * mark user read specified feed
     * input data param format: [FeedId(L)]
     * @return 1 if success
     */
    def markFeedsRead = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val feedIds = param.as[List[Long]]
        feedIds.foreach(feedId=>{
            userDAO.markUserReadFeed(uid,feedId)
            Logger.info(s"USER[${user.userId}] mark feed $feedId all read ")
        })
        Ok("1").as("application/json")
    }

    /**
     * mark user read specified feed
     * input data param format: FeedId(L)
     * @return 1 if success
     */
    def markFeedRead = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val feedId = (param \ "FeedId").as[Long]
        userDAO.markUserReadFeed(uid,feedId)

        Logger.info(s"USER[${user.userId}] mark feed $feedId all read ")
        Ok("1").as("application/json")
    }

    /**
     * save user reading preferred options
     * input data param format:
     * options:{"folderClose":{},"nav":true,"expanded":false,"mode":"all",
     *          "sort":"newest","hideEmpty":false,"scrollRead":false}
     * @return 1 if success
     */
    def saveOptions() = AuthAction { (user,request)  =>
        val uid = user.userId
        val prefData = request.body.toString
        val setting = User( uid, user.email, prefData )
        userDAO.updateUser(setting)
        Logger.info(s"USER[${user.userId}] saveOptions $prefData ")
        Ok("1").as("application/json")
    }


    /**
     * mark user starred specified story
     * input data param format: [{StoryId:0,FeedId:0}]
     * @return 1 if success
     */
    def markStar = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val storyId = (param \ "StoryId").as[Long]
        val feedId = (param \ "FeedId").as[Long]
        val star = (param \ "Star").as[Int]
        userDAO.updateUserStoryLike(uid,feedId,storyId,star)
        Logger.info(s"USER[${user.userId}] markStar $storyId ")
        Ok("1").as("application/json")
    }

    /**
     * append a specific story stat
     * input data param format: [{StoryId:0,FeedId:0}]
     * @return 1 if success
     */
    def appendStoryStats = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val slist = param.as[List[JsObject]]
        val stats = slist.map(item => {
            val storyId = (item \ "StoryId").as[Long]
            val feedId = (item \ "FeedId").as[Long]
            val content = (item \ "Content").as[String]
            val action =  (item \ "Action").as[String]
            val ts = (item \ "TimeStamp").as[Long]
            UserActionStat(
                new Date(ts),
                action,
                uid,
                feedId,
                storyId,
                content
            )
        })
        userDAO.appendUserActStats(stats)
        Logger.info(s"USER[${user.userId}] append  ${stats.size} stats")
        Ok("1").as("application/json")
    }

    /**
     * get suggested people and feed to follow using user query
     * @return a list of suggestions
     */
    def getAddSuggestion = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val query = (param \ "query") .as[String]

        val items:List[String] = userDAO.getSuggestedUsersToFollow(query) ++
            feedDAO.getSuggestedFeedsToFollow(query) ++ List(query)

        val suggestion = Json.obj(
            "items" -> Json.toJson(items)
        )

        Ok(suggestion).as("application/json")
    }

    /**
     * Arrage feed source accepts user's re-arrangement of his subscription
     * and apply that to user's subscription
     * input is :
     * change = [{
            'XmlUrl': opml.XmlUrl,
            'Folder': opml.Folder,
            'Title': opml.Title,
            'Delete': opml.Delete
        }];
     * @return
     */
    def arrangeFeedSource = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val changes = (param \ "changes") .as[List[JsObject]]
        val opmlChanges:List[OpmlChange] = changes.map{change=>
            val xmlUrl = (change \ "XmlUrl") .as[String]
            val folder = (change \ "Folder") .as[String]
            val title = (change \ "Title") .as[String]
            val delete = (change \ "Delete") .as[Boolean]
            OpmlChange(xmlUrl,folder,title,delete)
        }

        userDAO.applyOpmlChanges(user.userId, opmlChanges)
        Ok("1").as("application/json")
    }

    /**
    * Server side will return all 3 stats, probably in different format
    * {
    * "monthly":{
    *   "posted":[
    *       {"t":"","count"}
    *       ...
    *    ],
    *   "read":[
    *       {"t":"","count"}
    *       ...
    *    ]
    *  },
    *  ...weekly...daily
    *  }
    *
    *  "topRead":[
    *   {FeedId,Title,Read,ReadPercent,LastUpdate,ItemPerDay}
    *   ...
    *  ],
    *  "topStar"...
    **/
    /**TODO: change it to GET method, and above
     * load user monthly reading stats
     * @return the structure described as above
     */
    def loadMonthlyReadStats = AuthAction { (user,request)  =>
        val hists = userDAO.getUserLastMonthStatsHistograms(user.userId)
        val topReads = userDAO.getUserLastMonthReadStats(user.userId)
        val topStars = userDAO.getUserLastMonthStarStats(user.userId)

        val result = Json.obj(
            "Monthly" -> Json.toJson(hists(0)),
            "Weekly" -> Json.toJson(hists(1)),
            "Daily" -> Json.toJson(hists(2)),
            "TopRead" -> Json.toJson(topReads),
            "TopStar" -> Json.toJson(topStars)
        )

        Ok(result).as("application/json")
    }

    /**
     * load user monthly feed stats
     * @return similar strucutre as described
     */
    def loadMonthlyFeedStats = AuthAction { (user,request)  =>

        val activeFeeds = userDAO.getUserLastMonthActiveFeedStats(user.userId)
        val inActiveFeeds = userDAO.getUserInActiveFeedStats(user.userId)

        val result = Json.obj(
            "ActiveFeeds" -> Json.toJson(activeFeeds),
            "InActiveFeeds" -> Json.toJson(inActiveFeeds)
        )

        Ok(result).as("application/json")
    }

    /**
     * preview the feed stories
     * input data param format: {url: xmlUrl}
     * @return a list of stories of the feed
     *         format: {FeedUrl:xmlUrl, Stories:[Story]}
     */
    def previewSubscription = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val url = (param \ "url") .as[String]

        //TODO: match url with or without /
        //val feedStories: Iterable[Story] = feedDAO.getFeedStories(url)
        val feedStories: Iterable[Story] = getMineUserFromUrl(url) match {
            case Some(thatUser) => //User's Star share
                val starStories = userDAO.getUserStarStories(thatUser.userId)
                starStories.map{ story=>
                    story.copy(feedId = -thatUser.userId, id = -story.id)
                }
                starStories
            case None => // ordinary feed
                feedDAO.getFeedStories(url)
        }
        if (feedStories.nonEmpty) {
            val subscriptionContent = Json.obj(
                "FeedUrl" -> JsString(url),
                "Stories" -> Json.toJson(feedStories.map(Json.toJson(_)))
            )
            Logger.info(s"USER[${user.userId}] previewSubscription ${feedStories.size} stories ")
            Ok(subscriptionContent).as("application/json")
        } else {
            //TODO: async???
            Await.result(feedDAO.createOrUpdateFeed(url), 10 seconds)
            val newFeedStories: Iterable[Story] = feedDAO.getFeedStories(url)
            val subscriptionContent = Json.obj(
                "FeedUrl" -> JsString(url),
                "Stories" -> Json.toJson(newFeedStories.map(Json.toJson(_)))
            )
            Logger.info(s"USER[${user.userId}] previewSubscription ${newFeedStories.size} new stories ")
            Ok(subscriptionContent).as("application/json")
        }

    }

    /**
     * add feed to user's subscription list
     * input data format: {url: xmlUrl}
     * @return 1 if successfully subscribed
     */
    def addSubscription() = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val url = (param \ "url") .as[String]
        val folder = (param \ "folder") .as[String]

        getMineUserFromUrl(url) match {
            case Some(thatUser) => //User's Star share
                userDAO.setUserFollow(user.userId,thatUser.userId)
                Ok("1").as("application/json")
            case None => // ordinary feed
                val uid = user.userId
                feedDAO.getRawOutlineFromFeed(url) match {
                    case Some(o) =>
                        userDAO.addOmplOutline(uid, o, folder)
                        val feed = feedDAO.loadFeedFromUrl(url).get
                        val userStat = UserStat(uid, feed.feedId, 0, 0, 0, "")
                        userDAO.insertUserStat(userStat)
                        userDAO.insertUserFeed(uid, feed.feedId)
                        Logger.info(s"USER[$uid] addSubscription ${o.xmlUrl} ")
                        Ok("1").as("application/json")
                    case None =>
                        Logger.error(s"USER[${user.userId}] addSubscription error, no such feed")
                        BadRequest
                }
        }
    }

    /**
     * remove feed from user's subscription list
     * input data format: {url:xmlUrl}
     * @return 1 if successfully removed
     */
    def removeSubscription() = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val url = (param \ "url") .as[String]
        userDAO.removeOmplOutline(user.userId, url)
        feedDAO.getRawOutlineFromFeed(url) match {
            case Some(o) =>
                val feed = feedDAO.loadFeedFromUrl(url).get
                userDAO.removeUserFeed(user.userId, feed.feedId)
        }
        Logger.info(s"USER[${user.userId}] removeSubscription $url ")
        Ok("1").as("application/json")
    }

    /**
     * export user's subscription to opml file format
     * @return xml file content
     */
    def exportOPML = AuthAction { (user,request)  =>
        val opml = userDAO.getUserOpml(user.userId).get
        Logger.info(s"USER[${user.userId}] exportOPML ")
        Ok(opml.toXml).as("text/html")
    }

    /**
     * import user's subscrption with a opml file
     * existing subscription will be merged with this opml
      * @return 1 if successfully imported
     */
    def importOPML = Action(parse.multipartFormData) { request =>
        getCurrentUser(request) match {
            case Some(user) =>
                //TODO: validation maybe, required!!!
                request.body.file("file").map { opmlfile =>
                    //val bb = new SerialBlob(FileUtils.readFileToByteArray(opmlfile.ref.file))
                    //val os = new OpmlStorage(user.userId, bb)
                    //userDAO.saveOpmlStorage(os) //TODO: should merge OPML instead of overwriting
                    //TODO: probalby don't need to read to memory , just stream to target file
                    val content_type = opmlfile.contentType
                    content_type match {
                        case Some("text/xml") | Some("application/xml") =>
                            val xmlContent = FileUtils.readFileToString(opmlfile.ref.file,"UTF-8")
                            val opml = OpmlStorage(user.userId, xmlContent).toOpml
                            val mergedOpml = userDAO.mergeWithUserOpml(opml)
                            FeedFactory.newFeeds(mergedOpml).foreach{newFeed=>
                                val savedFeed:Feed=feedDAO.verifyAndCreateFeed(newFeed)
                                userDAO.setUserFeedStat(user.userId, savedFeed.feedId)
                                userDAO.setUserFeed(user.userId, savedFeed.feedId)
                            }
                            Logger.info(s"USER[${user.userId}] importOPML merged new feeds")
                            Ok("1").as("application/json")
                        case _ =>
                            Ok("0").as("application/json")
                    }

                }.getOrElse(BadRequest)
            case _ => BadRequest
        }

    }

    /**
     * import user's subscription through a json param
     * @return 1 if successful
     */
    def uploadOPML = AuthAction { (user,request)  =>
        val jsonparams = request.body.asJson.get
        val feedlist = (jsonparams \ "Opml").as[List[JsObject]]
        val opmlOutlines = JsObject2OpmlOutlines(feedlist)
        val opml = Opml(user.userId, opmlOutlines)
        val mergedOpml = userDAO.mergeWithUserOpml(opml)
        FeedFactory.newFeeds(mergedOpml).map{newFeed=>
            val savedFeed = feedDAO.verifyAndCreateFeed(newFeed)
            userDAO.setUserFeedStat(user.userId, savedFeed.feedId)
            userDAO.setUserFeed(user.userId, savedFeed.feedId)
        }
        Logger.info(s"USER[${user.userId}] uploadOPML merged new feeds")
        Ok("1").as("application/json")
    }

    def verifySubscription() = AuthAction { (user,request)  =>
        val uid = user.userId
        val opml = userDAO.getUserOpml(uid).get
        val allFeedUrls = opml.allFeedsUrl
        allFeedUrls.foreach{ url=>
            val feed = feedDAO.loadFeedFromUrl(url).get
            val userStatOption = userDAO.getUserStat(uid,feed.feedId,0)
            userStatOption match {
                case None =>
                    val userStat = UserStat(uid, feed.feedId, 0, 0, 0, "")
                    userDAO.insertUserStat(userStat)
                    Logger.info(s"USER[$uid] created stat for feed $url ")
                case Some(us) =>
            }

        }
        Ok("1").as("application/json")
    }

    /*NOT GOING TO BE IMPLEMENTED */
    def feedHistory = Action { request =>
        NotImplemented
    }

    def charge = Action { request =>
        NotImplemented
    }

    def account = Action { request =>
        NotImplemented
    }

    def unCheckout = Action { request =>
        NotImplemented
    }

    def deleteAccount() = Action { request =>
        NotImplemented
    }

    def getFee = Action { request =>
        NotImplemented
    }

}