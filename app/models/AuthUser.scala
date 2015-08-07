package models

import java.util.Date
import scala.xml.XML
import scala.xml.Elem
import mining.util.DirectoryUtil
import scala.collection.mutable.ListBuffer
import play.api.mvc.Request
import mining.io.slick.SlickUserDAO
import scala.slick.driver.H2Driver
import mining.io.User


case class AuthUser ( id:String, email:String, name:String, apiKey:String )

object AuthUser{
  System.setProperty("runMode", "test")
  val userDAO = SlickUserDAO(H2Driver)
  
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
      request.headers.get(apiKey).flatMap(apiKey =>
      userDAO.getUserById(apiKey))
    }
  }
  
}


    
    
