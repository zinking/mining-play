package controllers

import play.api._
import play.api.mvc._
import java.text.SimpleDateFormat
import java.util.Date
import securesocial.core.{IdentityId, UserService, Identity, Authorization}
import models.User
import models.WithProvider

object Application extends Controller with securesocial.core.SecureSocial {
	
  def index = SecuredAction(WithProvider("google")) { implicit request =>
    val identity = request.user
    val msgs = Seq( )
      
    request.user match {
      case id: Identity => {
        val reader = User( id )
        Ok( views.html.index( reader ,msgs ) )
      }
      case _ => 
        Ok( "hello welcome" ) //default page not logged in
    }
    
  }
  
  
  def default = Action{ request =>
    NotImplemented
  }

}




