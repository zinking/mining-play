package controllers

import play.api._
import play.api.mvc._
import securesocial.core.{IdentityId, UserService, Identity, Authorization}
import models.User
import models.WithProvider
import models.WithProvider
import mining.io.slick._
import mining.io._
import javax.sql.rowset.serial.SerialBlob
import mining.io.OpmlStorage
import java.io.FileInputStream
import org.apache.commons.io.FileUtils
import play.api.libs.json
import play.api.libs.json._

object UserController extends Controller with securesocial.core.SecureSocial {
  System.setProperty("runMode", "test")
  val userDAO = SlickUserDAO(H2Driver)
  val feedDAO = SlickFeedDAO(H2Driver)
  
  def getContents( data:String ) = UserAwareAction { request => 
    //INPUT: LIST{Feed:xmlurl,Story:Id}
    request.user match {
      case Some(user) => {
        val jcontents = Json.parse(data).as[List[JsObject]]
        val storyContents = jcontents.map( ri =>
            JsObject(
                "Id"->(ri\"Id").as[JsString]::
                "Content"->feedDAO.getStoryContentById( (ri\"Feed").as[JsString] )::
                Nil
                )
            )
          Ok( "1" ).as("text/html")
      }
    }
    NotFound
  }

   def getFeed( c:String ) = UserAwareAction { request => 
   //get star stories of a user, with cursor/offset
    request.user match {
      case Some(user) => {
        val uid = user.email.get
        val stars = userDAO.getUserStarStories(uid)
        val starStories = stars.map( feedDAO.getStoryById(_.storyId))

         Ok( Json.toJson(
        		  Map( 
        		      "Cursor"  -> JsString(c),
        		      "Stories" -> Json.toJson( starStories.map( Story2JsObject(_))),
        		      "stars"   -> Json.toJson( stars.map( JsString(_)))
        		  )
              ) ).as("application/json")
      }
    }
    NotFound
  }
  
 def getFeed( f:String, c:String ) = UserAwareAction { request => 
   //get stories of a feed, with cursor/offset
    request.user match {
      case Some(user) => {
        val uid = user.email.get
        val stories = feedDAO.getFeedStories(f) //Question here is how is user's read/unread info dealt with
        val stars = userDAO.getUserStarStories(uid)
	     
         Ok( Json.toJson(
        		  Map( 
        		      "Cursor"  -> JsString(c),
        		      "Stories" -> Json.toJson( stories.map( Story2JsObject(_))),
        		      "stars"   -> Json.toJson( stars.map( JsString(_)))
        		  )
              ) ).as("application/json")
      }
    }
    NotFound
  }

    def OpmlOutline2JsObject(  children:List[JsObject],node:OpmlOutline):JsObject = {
      new JsObject(
		      "Title"->JsString(node.title )::
		      "XmlUrl" ->JsString(node.xmlUrl)::
		      "Type" -> JsString(node.outlineType)::
		      "Text" -> JsString(node.text)::
		      "HtmlUrl" -> JsString(node.htmlUrl)::
		      "Outline"-> JsArray(children)::
		      Nil
		  )
	}
    /*
    {"Id":"http://flex1.com/blog/entry3","Title":"Title1","Link":"http://flex1.com/blog/entry3","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" }
    */
    def Story2JsObject ( node:Story):JsObject = {
      new JsObject(
    		  "Id"->JsString(node.link)::
    		  "Title"->JsString(node.title)::
    		  "Link"->JsString(node.link)::
    		  "Updated"->JsString(node.updated.toString)::
    		  "Date"->JsString(node.published.toString)::
    		  "Author"->JsString(node.author)::
    		  "Summary"->JsString(node.description)::
    		  "Content"->JsString(node.content)::
    		  Nil
          )
    }
    
    /*
    {"Title":"Flex Title1", "Url":"http://flex1.com/rss", "Type":"rss", "Text":"FLEX TEXT T1",  "Image":"http://www.favicon.co.uk/ico/3908.png",
			"Updated":"2014-03-03", "NextUpdate":"2014-03-03", "Date":"2014-03-03" },
    */
    def Feed2JsObject( node:OpmlOutline):JsObject={
      new JsObject(
    		  "Title"->JsString(node.title)::
    		  "Url"->JsString(node.xmlUrl)::
    		  "Type"->JsString(node.outlineType)::
    		  "Text"->JsString(node.text)::
    		  Nil
          )
    }
  
  def listFeeds = UserAwareAction { request => 
    request.user match {
      case Some(user) => {
        val uid = user.email.get
        val opml = userDAO.getOpmlById( uid )
    	  val opmllist = opml.outline.foldLeft[List[JsObject]]( List[JsObject]() )(( acc, node ) =>{
	    	val subOutlines = outlines.outline
	    	val subopmllist = outline2.foldLeft[List[JsObject]]( List[JsObject]() )(( acc2, node2 ) =>{
	    		val nid2 = OpmlOutline2JsObject( List[JsObject](), node2 )
	    		acc2 :+ nid2
	    	})
	    	val nid = JsObject2OpmlOutline(subopmllist, node )
	        acc :+ nid
	     })
	     
	     val stories = feedDAO.getOpmlStories(opml)
	     val feeds = opml.allFeeds
	     val stars = userDAO.getUserStarStories(uid)
	     
         Ok( Json.toJson(
        		  Map( 
        		      "Opml"    -> opmllist,
        		      "Stories" -> Json.toJson( stories.map( Story2JsObject(_))),
        		      "feeds"   -> Json.toJson( feeds.map( Feed2JsObject(_)) ),
        		      "stars"   -> Json.toJson( stars.map( JsString(_)))
        		  )
              ) ).as("application/json")
      }
    }
    NotFound
  }
  
  def markRead = Action{ request =>
    NotImplemented
  }
  
  def markUnread = Action{ request =>
    NotImplemented
  }  
  
  def saveOptions( options:String ) = UserAwareAction { request =>
    //INPUT options:{"folderClose":{},"nav":true,"expanded":false,"mode":"all","sort":"newest","hideEmpty":false,"scrollRead":false}
	request.user match {
      case Some(user) => {
        val joptions = Json.parse(options).as[JsObject]
        val setting = Setting( user.email.get, (joptions\"sort").as[String] , 
        				(joptions\"mode").as[String] ,(joptions\"hideEmpty").as[String] )
        userDAO.saveUserSetting( setting )
        Ok( "1" ).as("application/json")
      }
    }
    NotFound
  }
  
  def setStar( feed:String, story:String, del:String ) = UserAwareAction { request => 
    //INPUT {feed:xmlUrl, story:storyId, del: '' : '1' //TODO: I don't like these magic numbers, they should be adapted to meaningful things
    request.user match {
      case Some(user) => {
          val uid = user.email.get
          userDAO.setUserStarStory( uid, story,  
        		  del match{
			        case "" => ""
			        case "1" => "STAR"
			      }
              )
          Ok( "1" ).as("application/json")
      }
    }
    NotFound
  }
  
  def addSubscription( url:String) = UserAwareAction { request =>
    //1. update the feed in the system
    //2. record the feed in user's inventory
    request.user match {
      case Some(user) => {
    	  val feedp = feedDAO.createOrUpdateFeed(url)
    	  userDAO.addOmplOutline( feedp.feed )
          Ok( Json.toJson(
        		  Map( "data"-> "Subscripton Added" )
              ) ).as("application/json")
      }
    }
    NotFound
  }
  
  def exportOPML = UserAwareAction { request =>
    request.user match {
      case Some(user) => {
    	  val opml = userDAO.getOpmlById( user.email.get ).get
          Ok( opml.toXml ).as("text/html")
      }
    }
    NotFound
  }
  
  //this method deals with file input
  def importOPML = UserAwareAction(parse.multipartFormData) { implicit request =>
    request.user match {
      case Some(user) => {
         request.body.file("file").map{ opmlfile =>
            val bb = new SerialBlob( FileUtils.readFileToByteArray( opmlfile.ref.file ) )
         	val os = new OpmlStorage( user.email.get, bb )
            userDAO.saveOpmlStorage(os)
            Ok( "1" ).as("application/json")
         }
      }
    }
    NotFound
  }
  
  def JsObject2OpmlOutline(  children:List[OpmlOutline],node:JsObject):OpmlOutline = {
	  new OpmlOutline(children,  (node\"title").as[String], (node\"xmlUrl").as[String], 
	    (node\"type").as[String], (node\"text").as[String], (node\"htmlUrl").as[String])
	}
  //this method deals with json input
  //POST opml=>jsonstring
  def uploadOPML( opml:String) = UserAwareAction { request =>
    
     request.user match {
      case Some(user) => {
         val feedlist = Json.parse(opml).as[List[JsObject]]
         val result = feedlist.foldLeft[List[OpmlOutline]]( List[OpmlOutline]() )(( acc, node ) =>{
	    	val outline2 = (node \ "Outline").as[List[JsObject]]
	    	val result2 = outline2.foldLeft[List[OpmlOutline]]( List[OpmlOutline]() )(( acc2, node2 ) =>{
	    		val nid2 = JsObject2OpmlOutline( List[OpmlOutline](), node2 )
	    		acc2 :+ nid2
	    	})
	    	val nid = JsObject2OpmlOutline(result2, node )
	        acc :+ nid
	     })
	     val opmlresult = Opml(user.email.get, result)
	     userDAO.saveOpml( opmlresult )
         Ok( "1" ).as("application/json")
      }
    }
    NotFound
  }
  
  /*NOT GOING TO BE IMPLEMENTED */
  def feedHistory = Action{ request =>
    NotImplemented
  }
  
  def charge = Action{ request =>
    NotImplemented
  }
  
  def account = Action{ request =>
    NotImplemented
  }
  
  def unCheckout = Action{ request =>
    NotImplemented
  }
  
  def deleteAccount = Action{ request =>
    NotImplemented
  }
  
  def getFee = Action{ request =>
    NotImplemented
  }
  
}
