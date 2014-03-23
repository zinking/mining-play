package models

import java.util.Date

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