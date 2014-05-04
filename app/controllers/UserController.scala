package controllers

import play.api._
import play.api.mvc._
import securesocial.core.{IdentityId, UserService, Identity, Authorization}
import models.User
import models.WithProvider
import scala.slick.driver.H2Driver
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
  
  userDAO.manageDDL
  feedDAO.manageDDL
  
  def getContents(  ) = UserAwareAction { request => 
    //INPUT: LIST{Feed:xmlurl,Story:Id}
    //OUTPUT: LIST{content string}
    //val data = request.getQueryString("data").get
    request.user match {
      case Some(user) => {
        val param = request.body.asJson.get
        val jcontents = param.as[Seq[JsObject]]
        //val storyContents = jcontents.map( ri =>
	    //        JsObject(
	    //            "Id"->(ri\"Story").as[JsString]::
	    //            "Content"->JsString( feedDAO.getStoryContentByLink( (ri\"Story").as[String] ) )::
	    //            Nil
	    //        )
        //    )
        val storyContents = jcontents.map( ri => JsString( feedDAO.getStoryContentByLink( (ri\"Story").as[String] ) ) )
        Ok( Json.toJson( storyContents )).as("application/json")
      }
      
      case _ => NotFound
    }
    
  }

   def getStars(  ) = UserAwareAction { request => 
   //get star stories of a user, with cursor/offset
    request.user match {
      case Some(user) => {
        val uid = user.email.get
        val param = request.body.asJson.get
        val c = ((param\"c").as[String]).toInt
        //val f = (param\"f").as[String]
        val stars = userDAO.getUserStarStories(uid, pageno=c)
        val starStories = stars.map( s =>
            feedDAO.getStoryByLink(s.link)
        )
         Ok( Json.toJson(
        		  Map( 
        		      "Cursor"  -> param\"c",
        		      "Stories" -> Json.toJson( starStories.map( Story2JsObject(_))),
        		      "stars"   -> Json.toJson( stars.map( s=> JsString( s.link )))
        		  )
              ) ).as("application/json")
      }
      case _ => NotFound
    }
    
  }
  
 def getFeed(  ) = UserAwareAction { request => 
   //get stories of a feed, with cursor/offset
    request.user match {
      case Some(user) => {
        val uid = user.email.get
        val param = request.body.asJson.get
        val c = ((param\"c").as[String]).toInt
        val f = (param\"f").as[String]
        val stories = feedDAO.getFeedStories(f, pageNo=c) //Question here is how is user's read/unread info dealt with
        val stars = userDAO.getUserStarStories(uid, pageno=c)
         Ok( Json.toJson(
        		  Map( 
        		      "Cursor"  -> JsString( (c+1).toString  ),
        		      "Stories" -> Json.toJson( stories.map( Story2JsObject(_))),
        		      "stars"   -> Json.toJson( stars.map( s=> JsString(s.link)))
        		  )
              ) ).as("application/json")
      }
      case _ => NotFound
      
    }
    
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
    		  "Date"->JsNumber(node.published.getTime/1000.0f)::
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
        val opml = userDAO.getOpmlById( uid ).getOrElse( new Opml( uid, Nil) )
    	  val opmllist = opml.outline.foldLeft[List[JsObject]]( List[JsObject]() )(( acc, node ) =>{
	    	val subOutlines = node.outline
	    	val subopmllist = subOutlines.foldLeft[List[JsObject]]( List[JsObject]() )(( acc2, node2 ) =>{
	    		val nid2 = OpmlOutline2JsObject( List[JsObject](), node2 )
	    		acc2 :+ nid2
	    	})
	    	val nid = OpmlOutline2JsObject(subopmllist, node )
	        acc :+ nid
	     })
	     
	     val stories = feedDAO.getOpmlStories(opml)
	     val feeds = opml.getAllOutlines
	     val stars = userDAO.getUserStarStories(uid)
	     
         Ok( Json.toJson(
        		  Map( 
        		      "Opml"    -> Json.toJson( opmllist ),
        		      "Stories" -> Json.toJson( stories.map( Story2JsObject(_))),
        		      "feeds"   -> Json.toJson( feeds.map( Feed2JsObject(_)) ),
        		      "stars"   -> Json.toJson( stars.map( s=> JsString( s.link )))
        		  )
              ) ).as("application/json")
      }
      case _ => NotFound
    }
    
  }
  
  def markRead = UserAwareAction { request =>
    //INPUT [{Feed(xmlurl) Story(storyId)}]
    request.user match {
      case Some(user) => {
          val uid = user.email.get
          val param = request.body.asJson.get
          val slist = param.as[List[JsObject]] 
          slist.foreach( item =>{
            val feed  = (item\"Feed").as[String]
            val story = (item\"Story").as[String]
            userDAO.saveUserReadStoryWithLink(uid, story, "READ")
          })
          
          Ok( "1" ).as("application/json")
      }
      case _ => NotFound
    }
  }
  
  def markUnread = UserAwareAction { request =>
    request.user match {
      case Some(user) => {
          val uid = user.email.get
          val param = request.body.asJson.get
          val slist = param.as[List[JsObject]] 
          slist.foreach( item =>{
            val feed  = (item\"Feed").as[String]
            val story = (item\"Story").as[String]
            userDAO.saveUserReadStoryWithLink(uid, story, "UNREAD")
          })
          
          Ok( "1" ).as("application/json")
      }
      case _ => NotFound
    }
  }  
  
  def saveOptions(  ) = UserAwareAction { request =>
    //INPUT options:{"folderClose":{},"nav":true,"expanded":false,"mode":"all","sort":"newest","hideEmpty":false,"scrollRead":false}
    //val options = request.getQueryString("options").get
	request.user match {
      case Some(user) => {
        //val joptions = Json.parse(options).as[JsObject]
        val uid = user.email.get
        val param = request.body.asJson.get
        val joptions = param \ "options"
        val setting = mining.io.User( uid,uid, (joptions\"sort").as[String] , (joptions\"mode").as[String] ,((joptions\"hideEmpty").as[Boolean]).toString )
        userDAO.saveUser( setting )
        Ok( "1" ).as("application/json")
      }
      case _ => NotFound
    }
  }
  
  def setStar(  ) = UserAwareAction { request => 
    //INPUT {feed:xmlUrl, story:storyId, del: '' : '1' //TODO: I don't like these magic numbers, they should be adapted to meaningful things
    //the namings of request parameter is not consistent TODO:
    //val data = request.getQueryString("data").get
    request.user match {
      case Some(user) => {
          val uid = user.email.get
          val param = request.body.asJson.get
          val feed  = (param\"feed").as[String]
          val story = (param\"story").as[String]
          val del   = (param\"del").as[String]
          userDAO.setUserStarStoryWithLink( uid, story,  del == "1" )
          Ok( "1" ).as("application/json")
      }
      case _ => NotFound
    }
    
  }
  
  def addSubscription = UserAwareAction { request =>
    //1. update the feed in the system
    //2. record the feed in user's inventory
    
    request.user match {
      case Some(user) => {//TODO: drastically simplified scenario
          //val url = request.getQueryString("url").get
          val url = request.body.asFormUrlEncoded.get("url")(0)
    	  val feedp = feedDAO.createOrUpdateFeed(url) //TODO: throw catch
    	  val uid = user.email.get
    	  userDAO.addOmplOutline( uid, feedp.outline )
          Ok( Json.toJson(
        		  Map( "data"-> "Subscripton Added" )
              ) ).as("application/json")
      }
      case _ => NotFound
    }
    
  }
  
  def exportOPML = UserAwareAction { request =>
    request.user match {
      case Some(user) => {
    	  val opml = userDAO.getOpmlById( user.email.get ).get
          Ok( opml.toXml ).as("text/html")
      }
      case _ => NotFound
    }
    
  }
  
  //this method deals with file input
  def importOPML = UserAwareAction(parse.multipartFormData) { request =>
    request.user match {
      case Some(user) => {//TODO: validation maybe
         request.body.file("file").map{ opmlfile =>
            val bb = new SerialBlob( FileUtils.readFileToByteArray( opmlfile.ref.file ) )
         	val os = new OpmlStorage( user.email.get, bb )
            userDAO.saveOpmlStorage(os)//TODO: should merge OPML instead of overwriting
            Ok( "1" ).as("application/json")
         }.getOrElse(NotFound)
         
      }
      case _ => NotFound
    }
    
  }
  
  def JsObject2OpmlOutline(  children:List[OpmlOutline],node:JsObject):OpmlOutline = {
	  new OpmlOutline(children,  (node\"Title").as[String], (node\"XmlUrl").as[String], 
	    (node\"Type").as[String], (node\"Text").as[String], (node\"HtmlUrl").as[String])
	}
  //this method deals with json input
  //POST opml=>jsonstring
  def uploadOPML(  ) = UserAwareAction{ request =>
     request.user   match {
      case  Some(user) => {
         val jsonparams = request.body.asJson.get
         val feedlist = (  jsonparams \ "opml" ).as[List[JsObject]] 
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
      case _ => NotFound
    }
    
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
