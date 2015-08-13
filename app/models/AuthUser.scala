package models

import java.util.Date
import scala.xml.XML
import scala.xml.Elem
import mining.util.DirectoryUtil
import scala.collection.mutable.ListBuffer
import play.api.mvc.Request
import mining.io.User

case class AuthUser ( 
    var userId:Long, 
    email:String, 
    name:String,
    pass:String,
    apiKey:String,
    lastLoginFrom:String,
    lastLoginTime:Date
)

object AuthUser{
  //since mocking frameworks on SCALA is not support mocking singleton
  //hacks are either required to change the design or ...
  //I'd rather putting the mocking logic here by myself
  var testUser:Option[User] = None;
  
  def getCurrentUser(request: Request[Object]) = {
    if( testUser != None ){
      testUser;
    }
    else{
      val apiKey = "mining-api-key"
      //request.headers.get(apiKey).flatMap(apiKey =>
      //userDAO.getUserById(1L))
      testUser
    }
  }
}