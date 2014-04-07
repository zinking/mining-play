package models

import java.util.Date
import scala.xml.XML
import scala.xml.Elem
import mining.util.DirectoryUtil
import scala.collection.mutable.ListBuffer
import play.libs.Json
import com.fasterxml.jackson.databind.JsonNode


case class User (
    id:String,
    email:String,
    read:Date,
    account:Int,
    created:Date
)

object User{
  var users = Set(
      User("1","1@1.1",new Date, 1, new Date)
  )
  def findAll = this.users.toList.sortBy( _.email )
}

case class OpmlOutline(
    outline:ListBuffer[OpmlOutline] = new ListBuffer[OpmlOutline],
    title:String,
    xmlUrl:String,
    outlineType:String,
    text:String,
    htmlUrl:String
){
  def addOutline( o:OpmlOutline){
    outline += o
  }
}

case class Opml(
    xmlName:String,
    version:String,
    title:String,
    outline:Seq[OpmlOutline]
)

object Opml{
	def loadSampleOpml():Array[OpmlOutline]={
		val tmpPath = DirectoryUtil.pathFromProject("conf", "user_opml.xml")
		val xml = XML.loadFile(tmpPath)
		
		val outlineSeq = xml \\ "outline"
		val outlineMap:scala.collection.mutable.Map[String, OpmlOutline] = scala.collection.mutable.Map.empty
		
		println( outlineSeq.length )
		
		// First pass, create all outline
		for {
		  outline <- outlineSeq
		  xmlurl  <- (outline \ "@xmlUrl")
		  title   <- (outline \ "@title") 
		  type1   <- (outline \ "@type")  
		  text    <- (outline \ "@text")  
		  htmlurl <- (outline \ "@htmlUrl")    
		} outlineMap(xmlurl.toString + title.toString ) = new OpmlOutline(
		    null,title.toString,xmlurl.toString,type1.toString,text.toString,htmlurl.toString
		)
		
		// Second pass, assign children
		for {
		  outline    <- outlineSeq
		  xmlurl     <- (outline \ "@xmlUrl")
		  title      <- (outline \ "@title" ) 
		  suboutline <- (outline \ "outline")
		  subxmlurl  <- (outline \ "@xmlUrl")
		  subtitle   <- (outline \ "@title" ) 
		} outlineMap(xmlurl.toString + title.toString).addOutline(
		    outlineMap(subxmlurl.toString + subtitle.toString)
		)
		
		println(outlineMap.size)
		
		outlineMap.values.toArray
	  
	}
}

case class UserOpml(
    id:String,
    opml:Seq[Byte]
)

object UserOpml{
  
}


    
    
