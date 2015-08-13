package models.slick

import models.AuthUser
import javax.inject.{Singleton, Inject}
import slick.driver.H2Driver.api._
import scala.concurrent.Future
import java.sql.Timestamp
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import mining.io.slick.SlickUtil


class SlickAuthUserDDL(db: Database){
  
    //Implicitly map j.u.Date to Time stamp for the following column definitions
  implicit def dateTime = MappedColumnType.base[Date, Timestamp](
    dt => new Timestamp(dt.getTime),
    ts => new Date(ts.getTime)
  )

  class AuthUserInfo(tag: Tag) extends Table[AuthUser](tag, "AUTHUSER_INFO") {
    def userId = column[Long]("USER_ID", O.PrimaryKey) 
    def email  = column[String]("EMAIL")
    def name   = column[String]("NAME")
    def pass   = column[String]("PASS")
    def apiKey = column[String]("API_KEY")
    def lastLoginFrom = column[String]("LAST_LOGIN_FROM")
    def lastLoginTime = column[Date]("LAST_LOGIN_Time")

    def * = (userId, email, name, pass, apiKey, lastLoginFrom, lastLoginTime) <> ((AuthUser.apply _).tupled, AuthUser.unapply)
  }
}

object SlickAuthUserDAO {
  def apply(db:Database) = new SlickAuthUserDAO(db)
}

class SlickAuthUserDAO(db: Database) extends SlickAuthUserDDL(db){
  
  val authUsers = TableQuery[AuthUserInfo]
  
  def manageDDL( ) = {
    val tablesMap = SlickUtil.tablesMap(db)
    if (!tablesMap.contains("AUTHUSER_INFO"))   db.run(authUsers.schema.create)
  }

  def updateAuthUser(user: AuthUser) : Future[Unit] ={
    db.run(authUsers.filter(_.userId === user.userId).update(user)).map(_ => ())
  } 

  def addNewUser(newUser: AuthUser): Future[Unit] ={
    db.run(authUsers += newUser).map(_ => ())
  }  
  
  def getUserById(userId: Long): Future[Option[AuthUser]] ={
    db.run(authUsers.filter(_.userId === userId).result.headOption) 
  }
  
  def getUserByEmail(email: String): Future[Option[AuthUser]] = {
    db.run(authUsers.filter(_.email === email).result.headOption) 
  }
  
  def authUser( email:String, hashedPass:String): Future[Option[AuthUser]] = {
    db.run(authUsers.filter( _.email === email ).filter( _.pass === hashedPass ).result.headOption )
  }
  
}