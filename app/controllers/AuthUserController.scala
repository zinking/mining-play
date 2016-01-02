package controllers

import java.util.Date

import mining.model.AuthUser
import play.api.mvc.Action
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import mining.io.UserFactory
import play.api.libs.json.{JsResultException, JsString, Json}

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
      case Some(verifiedUser) =>  Ok(views.html.login("Done"))
      case _ => Ok(views.html.login("Incorrect Credential"))
    }
  }
  
  def apiAuth=Action.async{ implicit request =>
    val param = request.body.asJson.get
    try{
      val email = (param \ "email") .as[String]
      val pass  = (param \ "pass")  .as[String]
    } catch{
      case e:JsResultException =>
        BadRequest
    }

    val email = (param \ "email") .as[String]
    val pass  = (param \ "pass")  .as[String]
    for {
      authUser <- authDAO.getUserByEmail(email)
    } yield authUser match {
      case Some(verifiedUser) =>
        val result = Json.obj( "apiKey" -> JsString(verifiedUser.apiKey) )
        Ok(result).as("application/json")
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

  def apiRegister=Action.async{ implicit request =>
    val param = request.body.asJson.get
    try{
      val email = (param \ "email") .as[String]
      val pass  = (param \ "pass")  .as[String]
    } catch{
      case e:JsResultException =>
        BadRequest
    }
    val email = (param \ "email") .as[String]
    val pass  = (param \ "pass")  .as[String]
    for {
      euser <- authDAO.getUserByEmail(email)
    } yield euser match {
      case Some(au) =>
        val result = Json.obj("error" -> JsString("EMAIL_EXISTS"))
        Ok(result).as("application/json")
      case None =>
        val newUser = AuthUser(0L,email,email,pass,"","",new Date)
        val registeredUser = authDAO.addNewUser(newUser)
        val newMiningUser = UserFactory.newUser(registeredUser.userId, registeredUser.email)
        userDAO.saveUser(newMiningUser)
        val result = Json.obj("info" -> JsString("REGISTER_SUCCESS"))
        Ok(result).as("application/json")
    }
  }
  
}