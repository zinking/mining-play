package models

import java.util.Date
import scala.xml.XML
import scala.xml.Elem
import mining.util.DirectoryUtil
import scala.collection.mutable.ListBuffer
import securesocial.core.Authorization
import securesocial.core.{IdentityId, UserService, Identity, Authorization}


case class User ( id:String, email:String )

object User{
  def apply( user:Identity ) = {
    new User( user.email.get, user.email.get  )
  }
}

case class WithProvider(provider: String) extends Authorization {
  def isAuthorized(user: Identity) = {
    user.identityId.providerId == provider
  }
}


    
    
