package controllers

import play.api._
import play.api.mvc._
import securesocial.core.{IdentityId, UserService, Identity, Authorization}
import models.User
import models.WithProvider
import models.WithProvider
import mining.io.slick._
import mining.io.Opml
import javax.sql.rowset.serial.SerialBlob
import mining.io.OpmlStorage
import java.io.FileInputStream
import org.apache.commons.io.FileUtils
import play.api.libs.json
import play.api.libs.json._
import mining.io.OpmlOutline

object UserController extends Controller with securesocial.core.SecureSocial {
  System.setProperty("runMode", "test")
  val userDAO = SlickUserDAO(H2Driver)
  val feedDAO = SlickFeedDAO(H2Driver)

  

  

  

  
  def getContents = Action{ request =>
    val result = """
    {
		"Id":"http://flex3.com/blog/entry1",
		"Content":"lorem posuoi akldj;falkjsdlfjal;kdsjflkajldkjfalk;sdj;fajklsdj;fakdsl"
	}
	"""
	Ok( result ).as("application/json")
  }
  
  def getFee = Action{ request =>
    NotImplemented
  }
  
  def getStars = Action{ request =>
    //-c GAE feature to fetch next 20 items
	val result = """
    {
		"Cursor":"PAGE1",
		"Stories":{
			"http://flex1.com/rss":[
				{"Id":"http://flex1.com/blog/entry1","Title":"Title1","Link":"http://flex1.com/blog/entry1","Created":"2014-03-03", "Published":"2014-03-03", 
				 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
				{"Id":"http://flex1.com/blog/entry2","Title":"Title1","Link":"http://flex1.com/blog/entry2","Created":"2014-03-03", "Published":"2014-03-03", 
				 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
				{"Id":"http://flex1.com/blog/entry3","Title":"Title1","Link":"http://flex1.com/blog/entry3","Created":"2014-03-03", "Published":"2014-03-03", 
				 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" }
			]
		},
		"Stars":{
			"http://flex1.com/blog/entry1":"2014-03-03",
			"http://flex1.com/blog/entry2":"2014-03-03",
			"http://flex1.com/blog/entry3":"2014-03-03"
		}
	}
	"""
	Ok( result ).as("application/json")
	
  }
  
  def getFeed = Action{ request =>
    val result = """
    {
		"Cursor":"PAGE1",
		"Stories":[
				{"Id":"http://flex1.com/blog/entry1","Title":"Title1","Link":"http://flex1.com/blog/entry1","Created":"2014-03-03 00:00:00", "Published":"2014-03-03 00:00:00", 
				 "Updated":"2014-03-03 00:00:00", "Date":"2014-03-03 00:00:00", "Author":"A1", "Summary":"S1",  "content":"C1" },
				{"Id":"http://flex1.com/blog/entry2","Title":"Title1","Link":"http://flex1.com/blog/entry2","Created":"2014-03-03 00:00:00", "Published":"2014-03-03 00:00:00", 
				 "Updated":"2014-03-03 00:00:00", "Date":"2014-03-03 00:00:00", "Author":"A1", "Summary":"S1",  "content":"C1" },
				{"Id":"http://flex1.com/blog/entry3","Title":"Title1","Link":"http://flex1.com/blog/entry3","Created":1395842385, "Published":1395842385, 
				 "Updated":1395842385, "Date":1395842385, "Author":"A1", "Summary":"S1",  "content":"C1" } //TIME IN EPOCH TODO: AGO
		],
		"Stars":{
			"http://flex1.com/blog/entry1":"2014-03-03",
			"http://flex1.com/blog/entry2":"2014-03-03",
			"http://flex1.com/blog/entry3":"2014-03-03"
		}
	}
	"""
	Ok( result ).as("application/json")
  }
  

  
  def listFeeds = Action{ request =>
    //NotImplemented
	//MEDIA COTENT is really intended for media
    val result = """
    {
      "Opml":[
	    {"Title":"FolderFlex",  "Type":"rss", "Outline":[
			{"Title":"Flex Title1", "XmlUrl":"http://flex1.com/rss", "Type":"rss", "Text":"FLEX TEXT T1", "HtmlUrl":"http://flex1.com/", "Outline":[] },
			{"Title":"Flex Title2", "XmlUrl":"http://flex2.com/rss", "Type":"rss", "Text":"FLEX TEXT T2", "HtmlUrl":"http://flex2.com/", "Outline":[] },
			{"Title":"Flex Title3", "XmlUrl":"http://flex3.com/rss", "Type":"rss", "Text":"FLEX TEXT T3", "HtmlUrl":"http://flex3.com/", "Outline":[] },
			{"Title":"Flex Title4", "XmlUrl":"http://flex4.com/rss", "Type":"rss", "Text":"FLEX TEXT T4", "HtmlUrl":"http://flex4.com/", "Outline":[] }] },
			
		{"Title":"Silver Title1", "XmlUrl":"http://Silver1.com/rss", "Type":"rss", "Text":"Silver TEXT T1", "HtmlUrl":"http://Silver1.com/", "Outline":[] }
      ],
      "Stories":{
		"http://flex1.com/rss":[
			{"Id":"http://flex1.com/blog/entry1","Title":"Title1","Link":"http://flex1.com/blog/entry1","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
			{"Id":"http://flex1.com/blog/entry2","Title":"Title1","Link":"http://flex1.com/blog/entry2","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
			{"Id":"http://flex1.com/blog/entry3","Title":"Title1","Link":"http://flex1.com/blog/entry3","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" }
		],
		"http://flex2.com/rss":[
			{"Id":"http://flex2.com/blog/entry1","Title":"Title1","Link":"http://flex2.com/blog/entry1","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
			{"Id":"http://flex2.com/blog/entry2","Title":"Title1","Link":"http://flex2.com/blog/entry2","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
			{"Id":"http://flex2.com/blog/entry3","Title":"Title1","Link":"http://flex2.com/blog/entry3","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" }
		],
		"http://flex3.com/rss":[
			{"Id":"http://flex3.com/blog/entry1","Title":"Title1","Link":"http://flex3.com/blog/entry1","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
			{"Id":"http://flex3.com/blog/entry2","Title":"Title1","Link":"http://flex3.com/blog/entry2","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
			{"Id":"http://flex3.com/blog/entry3","Title":"Title1","Link":"http://flex3.com/blog/entry3","Created":"2014-03-03", "Published":"2014-03-03", 
			 "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" }
		]
	  },
      "Feeds":[
		{"Title":"Flex Title1", "Url":"http://flex1.com/rss", "Type":"rss", "Text":"FLEX TEXT T1",  "Image":"http://www.favicon.co.uk/ico/3908.png",
			"Updated":"2014-03-03", "NextUpdate":"2014-03-03", "Date":"2014-03-03" },
		{"Title":"Flex Title2", "Url":"http://flex2.com/rss", "Type":"rss", "Text":"FLEX TEXT T2",  
			"Updated":"2014-03-03", "NextUpdate":"2014-03-03", "Date":"2014-03-03" },
		{"Title":"Flex Title3", "Url":"http://flex3.com/rss", "Type":"rss", "Text":"FLEX TEXT T3",  
			"Updated":"2014-03-03", "NextUpdate":"2014-03-03", "Date":"2014-03-03" },
		{"Title":"Flex Title4", "Url":"http://flex4.com/rss", "Type":"rss", "Text":"FLEX TEXT T4",  "Image":"http://www.favicon.co.uk/ico/4771.png",
			"Updated":"2014-03-03", "NextUpdate":"2014-03-03", "Date":"2014-03-03" }
       ],
	   "Stars":[
		"http://flex1.com/blog/entry1",
		"http://flex2.com/blog/entry1",
		"http://flex3.com/blog/entry1"
	   ],
      "UnreadDate":"2014-03-24",
      "UntilDate":"2014-08-24"
    }
    """
    
    
    Ok( result ).as("application/json")
  }
  
  def markRead = Action{ request =>
    NotImplemented
  }
  
  def markUnread = Action{ request =>
    NotImplemented
  }  
  
  def saveOptions = Action{ request =>
    //options:{"folderClose":{},"nav":true,"expanded":false,"mode":"all","sort":"newest","hideEmpty":false,"scrollRead":false}
	Ok("")
  }
  
  def setStar = UserAwareAction { request =>
    request.user match {
      case Some(user) => {
          Ok( "1" ).as("text/html")
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
  
  //this method deals with json input
  //POST opml=>jsonstring
  def uploadOPML( opml:String) = UserAwareAction { request =>
    def JsObject2OpmlOutline(  children:List[OpmlOutline],node:JsObject):OpmlOutline = {
      new OpmlOutline(children,  (node\"title").as[String], (node\"xmlUrl").as[String], 
        (node\"type").as[String], (node\"text").as[String], (node\"htmlUrl").as[String])
    }
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
  
}
