package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.data._
import play.api.data.Forms._
import models.AuthUser
import play.api.Play
import mining.io.slick.SlickUserDAO
import models.slick.SlickAuthUserDAO
import slick.driver.H2Driver
import java.security.MessageDigest
import javax.inject.{Singleton, Inject}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.H2Driver.api._

/**
 * @author awang
 */
class AuthUserController () extends Controller {
  val db = Database.forConfig("h2mem1")
  val authUserDAO = SlickAuthUserDAO(db)
  authUserDAO.manageDDL()
  
  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes)
  }
  
  val loginForm = Form(
      tuple(
        "email"    -> nonEmptyText,
        "password" -> nonEmptyText
      )
  )
  
  def login() = Action { request =>
    Ok(views.html.login(""))
  }
  
  def auth=Action.async( parse.form(loginForm) ){ implicit request =>
    val (email,pass) = request.body
    val hashedPass = md5(pass)
    for {
      authUser <- authUserDAO.authUser(email,hashedPass.toString())
    } yield authUser match {
      case Some(authUser) =>  Ok(views.html.login("Done"))
      case _ => Ok(views.html.login("Incorrect Credential"))
    }
  }
  
  def register() = Action{ request =>
     Ok(views.html.register(""))
  }
  
}