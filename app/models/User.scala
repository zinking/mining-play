package models

import java.util.Date
import scala.xml.XML
import scala.xml.Elem
import mining.util.DirectoryUtil
import scala.collection.mutable.ListBuffer
import securesocial.core.Authorization
import securesocial.core.{IdentityId, UserService, Identity, Authorization}


case class User (
    id:String,
    email:String,
    read:Date,
    account:Int,
    created:Date
)

object User{
  var users = Set(
      User("1","1@1.1",new Date, 1, new Date)
  )
  def findAll = this.users.toList.sortBy( _.email )
}





// An Authorization implementation that only authorizes uses that logged in using twitter
case class WithProvider(provider: String) extends Authorization {
  def isAuthorized(user: Identity) = {
    user.identityId.providerId == provider
  }
}


    
    
