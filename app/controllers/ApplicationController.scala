package controllers

import mining.io.User
import play.api.mvc.Controller
import play.api.mvc.Action
import models.AuthUser

object ApplicationController extends Controller {

  def index = Action { request =>
    AuthUser.getCurrentUser(request) match {
      case Some(user) => {
        Ok("hello")
      }
      case _ => Ok("hello welcome") //default page not logged in
    }

  }
  
  def default = Action { request =>
    NotImplemented
  }

}




