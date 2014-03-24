package controllers

import play.api._
import play.api.mvc._

object Application extends Controller{
  def index = Action { implicit request =>
    val user = models.User.findAll(0)
    val msgs = Seq( )
    val role = 1
    val stripekey = "l32jk"
    
    Ok( views.html.index(user,stripekey,msgs,role) )
     
  }
  
  def login = Action {
    Ok("Login Here")
  }
  
  def default = Action{ request =>
    NotImplemented
  }
}