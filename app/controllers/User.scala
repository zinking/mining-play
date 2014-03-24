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
    NotImplemented
  }
  
  def getFee = Action{ request =>
    NotImplemented
  }
  
  def getStars = Action{ request =>
    NotImplemented
  }
  
  def getFeed = Action{ request =>
    NotImplemented
  }
  
  def importOPML = Action{ request =>
    NotImplemented
  }
  
  def listFeeds = Action{ request =>
    //NotImplemented
    val result = """
    {
      "Opml":[
    	{"Title":"T1", "XmlUrl":"XmlT1", "Type":"rss", "Text":"TEXT T1", "HtmlUrl":"htmlT1", "Outline":[] },
        {"Title":"T2", "XmlUrl":"XmlT2", "Type":"rss", "Text":"TEXT T2", "HtmlUrl":"htmlT2", "Outline":[] },
        {"Title":"T3", "XmlUrl":"XmlT3", "Type":"rss", "Text":"TEXT T3", "HtmlUrl":"htmlT3", "Outline":[] }
      ],
      "Stories":[
    	{"Id":1,"Title":"Title1","Link":"Link1","Created":"2014-03-03", "Published":"2014-03-03", 
         "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
        {"Id":1,"Title":"Title1","Link":"Link1","Created":"2014-03-03", "Published":"2014-03-03", 
         "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" },
        {"Id":1,"Title":"Title1","Link":"Link1","Created":"2014-03-03", "Published":"2014-03-03", 
         "Updated":"2014-03-03", "Date":"2014-03-03", "Author":"A1", "Summary":"S1", "MediaContent":"MC1", "content":"C1" }
      ],
      "Feeds":[
        {"Url":"U1","Title":"Title1", "Updated":"2014-03-03", "Date":"2014-03-03"},
        {"Url":"U1","Title":"Title1", "Updated":"2014-03-03", "Date":"2014-03-03"},
        {"Url":"U1","Title":"Title1", "Updated":"2014-03-03", "Date":"2014-03-03"}
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
