package controllers

import actors.FeedRefreshManager
import akka.actor.ActorSystem
import com.google.inject.Inject
import mining.io.User
import mining.io.dao.UserDao
import mining.model.dao.AuthUserDao
import play.api.libs.json.JsResultException

import play.api.mvc._
import play.api.Logger

import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import javax.inject._


trait MiningController extends Controller {
    def db = scala.util.Properties.envOrElse("MININGENV", "prod" )
    val userDAO = UserDao()
    val authDAO = AuthUserDao()
    val apiKey = "mining"

    def AuthAction(f: (User,Request[AnyContent]) => Result): Action[AnyContent] = {
        Action { request =>
            getCurrentUser(request) match {
                case Some(user) =>
                    Logger.info("Calling action")
                    try{
                        f(user,request)
                    } catch {
                        case e:JsResultException=>
                            BadRequest

                    }
                case _ =>
                    val remoteIp = request.headers.get("Rmote_Addr")
                    val key = request.headers.get(apiKey)
                    Logger.info(s"$remoteIp Login Failed with apiKey $key")
                    NotFound
            }
        }
    }

    def getCurrentUser(request: Request[Object]): Option[User] = {
        request.headers.get(apiKey).flatMap { key =>
            authDAO.getUserByApikey(key).flatMap { authUser =>
                userDAO.getUser(authUser.userId)
            }
        }
    }
}

class ApplicationController () extends MiningController {
    def index = AuthAction { (user,request) =>
        Ok("hello")
    }

    def default = Action { request =>
        NotImplemented
    }
}



