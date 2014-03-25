package controllers

import play.api._
import play.api.mvc._
import models.Opml
import models.Story
import models.Feed
import play.libs.Json

object User extends Controller{
  
  def loginGoogle = Action{ request =>
    NotImplemented
  }
  
  def addSubscription = Action{ request =>
    NotImplemented
  }
  
  def deleteAccount = Action{ request =>
    NotImplemented
  }
  
  def exportOPML = Action{ request =>
    NotImplemented
  }
  
  def feedHistory = Action{ request =>
    NotImplemented
  }
  
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
    NotImplemented
  }
  
  def importOPML = Action{ request =>
    NotImplemented
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
    NotImplemented
  }
  
  def setStar = Action{ request =>
    NotImplemented
  }
  
  def uploadOPML = Action{ request =>
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
  
}
