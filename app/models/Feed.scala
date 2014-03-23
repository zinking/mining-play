package models

import java.util.Date
import scala.concurrent.duration._

case class Feed (
	url:String,
	title:String,
	updated:Date,
	checked:Date,
	nextUpdate:Date,
	link:String,
	hub:String,
	errors:Int,
	image:String,
	imageDate:Date,
	subscribed:Date,
	average:Duration,
	lastViewed:Date,
	noAds:Boolean
)

object Feed{
  var feeds = Set(
		  Feed("http://a.com","acom", new Date, new Date, new Date, "a.com/rss","hub", 1, "image", new Date, new Date, 2 hours, new Date, true ),
		  Feed("http://b.com","acom", new Date, new Date, new Date, "b.com/rss","hub", 1, "image", new Date, new Date, 2 hours, new Date, true ),
		  Feed("http://c.com","acom", new Date, new Date, new Date, "c.com/rss","hub", 1, "image", new Date, new Date, 2 hours, new Date, true )
      )
      
      
  def findAll = this.feeds.toList.sortBy( _.checked )
}