package controllers

import play.api._
import play.api.mvc._

import java.text.SimpleDateFormat;
import java.util.Date;
import securesocial.core.{IdentityId, UserService, Identity, Authorization}
import models.User

object Application extends Controller with securesocial.core.SecureSocial {
	
  def index = SecuredAction(WithProvider("google")) { implicit request =>
    //val user = models.User.findAll(0)
    val msgs = Seq( )
    val role = 1
    val stripekey = "l32jk"
    var reader:User = new User("1","1@1.1",new Date, 1, new Date)
      
    request.user match {
      case user: Identity => {
        reader = new User("1", user.email.getOrElse(""), new Date, 1, new Date)
      }
      case _ => //TBD: log error/thow exception
    }
    
    Ok( views.html.index( reader ,stripekey,msgs,role) )
  }
  
  def login = Action {
    Ok("Login Here")
  }
  
  def default = Action{ request =>
    NotImplemented
  }
  

}

// An Authorization implementation that only authorizes uses that logged in using twitter
case class WithProvider(provider: String) extends Authorization {
  def isAuthorized(user: Identity) = {
    user.identityId.providerId == provider
  }
}
