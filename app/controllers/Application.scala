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
    val msgs = Seq( )
    val role = 1
    val stripekey = "l32jk"
    var reader:User = new User("1","1@1.1",new Date, 1, new Date)
      
    request.user match {
      case user: Identity => {
        reader = new User("1", user.email.getOrElse(""), new Date, 1, new Date)
      }
      case _ => ??? //TODO: log error/thow exception
    }
    
    Ok( views.html.index( reader ,stripekey,msgs,role) )
  }
  
  
  def default = Action{ request =>
    NotImplemented
  }

}


