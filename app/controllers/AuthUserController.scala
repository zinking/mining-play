package controllers

import java.util.Date

import mining.model.AuthUser
import play.api.mvc.Action
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import mining.io.UserFactory
import play.api.libs.json.JsString
import play.api.libs.json.Json

/**
 * @author awang
 */
class AuthUserController () extends MiningController {
  val loginForm = Form(
      tuple(
        "email"    -> nonEmptyText,
        "password" -> nonEmptyText
      )
  )

  val registerForm = Form(
      tuple(
        "email"    -> nonEmptyText,
        "password" -> nonEmptyText
      )
  )
  
  def loginIndex() = Action { request =>
    Ok(views.html.login(""))
  }

  def registerIndex() = Action { request =>
    Ok(views.html.register(""))
  }

  def auth=Action.async( parse.form(loginForm) ){ implicit request =>
    val (email,pass) = request.body
    for {
      authUser <- authDAO.authUser(email,pass)
    } yield authUser match {
      case Some(authUser) =>  Ok(views.html.login("Done"))
      case _ => Ok(views.html.login("Incorrect Credential"))
    }
  }
  
  def apiAuth=Action.async{ implicit request =>
    val param = request.body.asJson.get
    val email = (param \ "email") .as[String]
    val pass  = (param \ "pass")  .as[String]
    for {
      authUser <- authDAO.getUserByEmail(email)//authUser(email,pass)
    } yield authUser match {
      case Some(authUser) => {
        val result = Json.obj( "apiKey" -> JsString(authUser.apiKey) )
        Ok(result).as("application/json")
      }
      case _ => NotFound
    }
  }

  def register=Action.async( parse.form(registerForm) ){ implicit request =>
    val (email,pass) = request.body
    for {
      euser <- authDAO.getUserByEmail(email)
    } yield euser match {
      case Some(au) => Ok(views.html.register(s"$email has already been taken"))
      case None => {
        val newUser = AuthUser(0L,email,email,pass,"","",new Date)
        val registeredUser = authDAO.addNewUser(newUser)
        val newMiningUser = UserFactory.newUser(registeredUser.userId, registeredUser.email) 
        userDAO.saveUser(newMiningUser)
        Ok(views.html.register("Done"))
      }
    }
  }
  
}