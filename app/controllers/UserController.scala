package controllers

import mining.io._
import mining.io.dao.FeedDao
import models.JsonUtils._
import org.apache.commons.io.FileUtils
import play.api.libs.json._
import play.api.mvc.Action
import play.api.Logger


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
            val StoryId = (ri \ "StoryId").as[Long]
            val storyContent = feedDAO.getStoryById(StoryId).content
            JsString(storyContent)
        })
        Logger.info(s"USER[${user.userId}] getContents ${jcontents.size} stories ")
        Ok(Json.toJson(storyContents)).as("application/json")
    }

    /**
     * get the stared stories 
     * input data params format: {C:pageNo}
     * @return user stared stories
     *         result format: { Cursor:pageNo, Stories:[Story], Stars:[htmlLink]}
     */
    def getStarStories = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val c = (param \ "C").as[Int]
        val stars = userDAO.getUserStarStories(uid, pageNo = c)
        val starStories = stars.map(s =>
            feedDAO.getStoryByLink(s.link))
        val starContent = Json.obj(
            "Cursor" -> c,
            "Stories" -> Json.toJson(starStories.map(Json.toJson(_))),
            "Stars" -> Json.toJson(stars.map(s => JsString(s.link))))

        Logger.info(s"USER[${user.userId}] getStarStories ${starStories.size} stories of page $c ")
        Ok(starContent).as("application/json")
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
        //TODO:Question here is how is user's read/unread info dealt with
        val stars = userDAO.getUserStarStories(uid, pageNo = c)
        val feedContent = Json.obj(
            "Cursor" -> JsNumber(c),
            "Stories" -> Json.toJson(stories.map(Json.toJson(_))),
            "Stars" -> Json.toJson(stars.map(s => JsString(s.link))))

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
        //TODO:Question here is how is user's read/unread info dealt with
        val stars = userDAO.getUserStarStories(uid, pageNo = c)
        val feedContent = Json.obj(
            "Cursor" -> JsNumber(c),
            "Stories" -> Json.toJson(stories.map(Json.toJson(_))),
            "Stars" -> Json.toJson(stars.map(s => JsString(s.link))))

        Logger.info(s"USER[${user.userId}] getFeedsStories ${stories.size} stories of page $c ")
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
        val feedStories: Iterable[Story] = feedDAO.getOpmlStories(opml)
        //val feedStoriesMap:Map[String,Iterable[Story]] = feedStories.groupBy(_.feedId.toString)
        val feeds = opml.getAllOutlines
        val feedIds = feedDAO.getOpmlFeeds(opml)
        val stars = userDAO.getUserStarStories(uid)

        //TODO: unreadDate concept.
        //the stories before this date cannot be marked as unread -- meaning they are all read
        //in the goread implementation , it tracks read/unread concept only within 2 weeks
        val indexContent = Json.obj(
            "Opml" -> Json.toJson(opmllist),
            "Stories" -> Json.toJson(feedStories.map(Json.toJson(_))),
            "Feeds" -> Json.toJson(feeds.map(Json.toJson(_))),
            "FeedIds" -> Json.toJson(feedIds.map(Json.toJson(_))),
            "Stars" -> Json.toJson(stars.map(s => JsString(s.link)))
        )

        Logger.info(s"USER[${user.userId}] listFeeds ${feedStories.size} stories, ${feeds.size} feeds ")
        Ok(indexContent).as("application/json")
    }

    /**
     * mark user read specified story
     * input data param format: [{StoryId:0}]
     * @return 1 if success
     */
    def markRead = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val slist = param.as[List[JsObject]]
        slist.foreach(item => {
            val StoryId = (item \ "StoryId").as[Long]
            userDAO.updateUserStoryRead(uid,StoryId,1)
        })
        Logger.info(s"USER[${user.userId}] markRead $slist ")
        Ok("1").as("application/json")
    }

    /**
     * mark user unread specified story
     * input data param format: [{StoryId:0}]
     * @return 1 if success
     */
    def markUnread = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val slist = param.as[List[JsObject]]
        slist.foreach(item => {
            val StoryId = (item \ "StoryId").as[Long]
            userDAO.updateUserStoryRead(uid,StoryId,0)
        })
        Logger.info(s"USER[${user.userId}] markUnread $slist ")
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
     * input data param format: [{StoryId:0}]
     * @return 1 if success
     */
    def markStar = AuthAction { (user,request)  =>
        //INPUT {feed:xmlUrl, story:StoryId, del: '' : '1'
        //TODO: I don't like these magic numbers, they should be adapted to meaningful things
        //the namings of request parameter is not consistent TODO:
        //val data = request.getQueryString("data").get
        val uid = user.userId
        val param = request.body.asJson.get
        //val feed = (param \ "Feed").as[String]
        //val story = (param \ "story").as[String]
        //val del = (param \ "Del").as[String]
        val StoryId = (param \ "StoryId").as[Long]
        userDAO.updateUserStoryLike(uid,StoryId,1)
        Logger.info(s"USER[${user.userId}] markStar $StoryId ")
        Ok("1").as("application/json")
    }

    /**
     * preview the feed stories
     * input data param format: {url: xmlUrl}
     * @return a list of stories of the feed
     *         format: {FeedUrl:xmlUrl, Stories:[Story]}
     */
    def previewSubscription() = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val url = (param \ "url") .as[String]

        // match url with or without /
        val feedStories: Iterable[Story] = feedDAO.getFeedStories(url)
        if (feedStories.nonEmpty) {
            val subscriptionContent = Json.obj(
                "FeedUrl" -> JsString(url),
                "Stories" -> Json.toJson(feedStories.map(Json.toJson(_)))
            )
            Logger.info(s"USER[${user.userId}] previewSubscription ${feedStories.size} stories ")
            Ok(subscriptionContent).as("application/json")
        } else {
            feedDAO.createOrUpdateFeed(url)
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
        //1. update the feed in the system
        //2. record the feed in user's inventory
        //TODO: drastically simplified scenario
        //val url = request.getQueryString("url").get
        //val url = request.body.asFormUrlEncoded.get("url").head
        val param = request.body.asJson.get
        val url = (param \ "url") .as[String]
        feedDAO.getRawOutlineFromFeed(url) match {
            case Some(opmlOutline) =>
                userDAO.addOmplOutline(user.userId, opmlOutline)
                Logger.info(s"USER[${user.userId}] addSubscription ${opmlOutline.xmlUrl} ")
                Ok("1").as("application/json")
            case None =>
                Logger.error(s"USER[${user.userId}] addSubscription error, no such feed")
                BadRequest
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
                            val xmlContent = FileUtils.readFileToString(opmlfile.ref.file)
                            val opml = OpmlStorage(user.userId, xmlContent).toOpml
                            val mergedOpml = userDAO.mergeWithUserOpml(opml)
                            FeedFactory.newFeeds(mergedOpml).map{newFeed=>
                                feedDAO.verifyAndCreateFeed(newFeed)
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
            feedDAO.verifyAndCreateFeed(newFeed)
        }
        Logger.info(s"USER[${user.userId}] uploadOPML merged new feeds")
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