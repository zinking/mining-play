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

    def getContents = AuthAction { (user,request)  =>
        //INPUT: LIST{Feed:xmlurl,Story:Id}
        //OUTPUT: LIST{content string}
        //val data = request.getQueryString("data").get
        val param = request.body.asJson.get
        val jcontents = param.as[Seq[JsObject]]
        //val storyContents = jcontents.map( ri =>
        //        JsObject(
        //            "Id"->(ri\"Story").as[JsString]::
        //            "Content"->JsString( feedDAO.getStoryContentByLink( (ri\"Story").as[String] ) )::
        //            Nil
        //        )
        //    )
        val storyContents = jcontents.map(ri => {
            //val story=(ri\"Story").as[String]
            //val storyContent=feedDAO.getStoryContentByLink(story)
            val StoryId = (ri \ "StoryId").as[Long]
            val storyContent = feedDAO.getStoryById(StoryId).content
            JsString(storyContent)
        })
        Ok(Json.toJson(storyContents)).as("application/json")
    }

    def getStars = AuthAction { (user,request)  =>
        //get star stories of a user, with cursor/offset
        val uid = user.userId
        val param = request.body.asJson.get
        val c = (param \ "C").as[Int]
        //val f = (param\"f").as[String]
        val stars = userDAO.getUserStarStories(uid, pageNo = c)
        val starStories = stars.map(s =>
            feedDAO.getStoryByLink(s.link))
        val starContent = Json.obj(
            "Cursor" -> c,
            "Stories" -> Json.toJson(starStories.map(Json.toJson(_))),
            "Stars" -> Json.toJson(stars.map(s => JsString(s.link))))
        Ok(starContent).as("application/json")
    }

    def getFeed = AuthAction { (user,request)  =>
        //get stories of a feed, with cursor/offset
        val uid = user.userId
        val param = request.body.asJson.get
        val c = (param \ "C").as[Int]
        val f = (param \ "F").as[String]
        val stories = feedDAO.getFeedStories(f, pageNo = c) //Question here is how is user's read/unread info dealt with
        val stars = userDAO.getUserStarStories(uid, pageNo = c)
        val feedContent = Json.obj(
            "Cursor" -> JsNumber(c+1),
            "Stories" -> Json.toJson(stories.map(Json.toJson(_))),
            "Stars" -> Json.toJson(stars.map(s => JsString(s.link))))
        Ok(feedContent).as("application/json")
    }


    def listFeeds = AuthAction { (user,request)  =>
        //INPUT [{Feed(xmlurl) Story(StoryId)}]
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
            //"Stories" -> FeedStoryMap2JsObject(feedStoriesMap),
            "Stories" -> Json.toJson(feedStories.map(Json.toJson(_))),
            "Feeds" -> Json.toJson(feeds.map(Json.toJson(_))),
            "FeedIds" -> Json.toJson(feedIds.map(Json.toJson(_))),
            "Stars" -> Json.toJson(stars.map(s => JsString(s.link)))
        )
        Ok(indexContent).as("application/json")
    }

    def markRead = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val slist = param.as[List[JsObject]]
        slist.foreach(item => {
            //val feed = (item \ "Feed").as[String]
            val StoryId = (item \ "StoryId").as[Long]
            userDAO.updateUserStoryRead(uid,StoryId,1)
        })
        Ok("1").as("application/json")
    }

    def markUnread = AuthAction { (user,request)  =>
        val uid = user.userId
        val param = request.body.asJson.get
        val slist = param.as[List[JsObject]]
        slist.foreach(item => {
            //val feed = (item \ "Feed").as[String]
            val StoryId = (item \ "StoryId").as[Long]
            userDAO.updateUserStoryRead(uid,StoryId,0)
        })
        Ok("1").as("application/json")
    }

    def saveOptions() = AuthAction { (user,request)  =>
        //INPUT options:{"folderClose":{},"nav":true,"expanded":false,"mode":"all","sort":"newest","hideEmpty":false,"scrollRead":false}
        //val options = request.getQueryString("options").get
        //val joptions = Json.parse(options).as[JsObject]
        val uid = user.userId
        val prefData = request.body.toString
        val setting = User( uid, user.email, prefData )
        userDAO.updateUser(setting)
        Ok("1").as("application/json")
    }

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
        Ok("1").as("application/json")
    }

    def previewSubscription() = AuthAction { (user,request)  =>
        //val url = request.body.asFormUrlEncoded.get("url").head
        val param = request.body.asJson.get
        val url = (param \ "url") .as[String]
        val feedp = feedDAO.createOrUpdateFeed(url)
        val feedStories: Iterable[Story] = feedDAO.getFeedStories(url)
        val subscriptionContent = Json.obj(
            "FeedUrl" -> JsString(url),
            "Stories" -> Json.toJson(feedStories.map(Json.toJson(_)))
        )
        Ok(subscriptionContent).as("application/json")
    }

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
                val subscriptionContent = Json.obj(
                    "data" -> "Subscripton Added"
                )
                Ok(subscriptionContent).as("application/json")
            case None =>
                Logger.error("Requested Subscription doesn't exist")
                BadRequest
        }
    }

    def removeSubscription() = AuthAction { (user,request)  =>
        val param = request.body.asJson.get
        val url = (param \ "url") .as[String]
        userDAO.removeOmplOutline(user.userId, url)
        val subscriptionContent = Json.obj(
            "data" -> "Subscripton Removed"
        )
        Ok(subscriptionContent).as("application/json")
    }

    def exportOPML = AuthAction { (user,request)  =>
        val opml = userDAO.getUserOpml(user.userId).get
        Ok(opml.toXml).as("text/html")
    }

    //this method deals with file input
    def importOPML = Action(parse.multipartFormData) { request =>
        getCurrentUser(request) match {
            case Some(user) =>
                //TODO: validation maybe, required!!!
                request.body.file("file").map { opmlfile =>
                    //val bb = new SerialBlob(FileUtils.readFileToByteArray(opmlfile.ref.file))
                    //val os = new OpmlStorage(user.userId, bb)
                    //userDAO.saveOpmlStorage(os) //TODO: should merge OPML instead of overwriting
                    //TODO: probalby don't need to read to memory , just stream to target file
                    val xmlContent = FileUtils.readFileToString(opmlfile.ref.file)
                    val opml = OpmlStorage(user.userId, xmlContent).toOpml
                    userDAO.setUserOpml(opml)
                    Ok("1").as("application/json")
                }.getOrElse(NotFound)
            case _ => NotFound
        }

    }

    //this method deals with json input
    //POST opml=>jsonstring
    def uploadOPML = AuthAction { (user,request)  =>
        val jsonparams = request.body.asJson.get
        val feedlist = (jsonparams \ "Opml").as[List[JsObject]]
        val opmlOutlineList = JsObject2OpmlOutlineList(feedlist)
        val opmlresult = Opml(user.userId, opmlOutlineList)
        userDAO.setUserOpml(opmlresult)
        feedDAO.createOrUpdateFeedOPML(opmlresult) //TODO: async ???
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