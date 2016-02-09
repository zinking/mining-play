package models

import mining.io._
import play.api.libs.json._

/**
 * Util for converting objects
 *
 * Created by awang on 5/12/15.
 */
object JsonUtils {
    implicit val storyWrites = new Writes[Story] {
        def writes(story: Story) = Json.obj(
            "Id"        -> story.id,
            "FeedId"    -> story.feedId,
            "Title"     -> story.title,
            "Link"      -> story.link,
            "Updated"   -> story.updated.toString,
            "Published" -> story.published.getTime / 1000.0f,
            "Author"    -> story.author,
            //"Content" -> JsString(node.content),
            "Summary"   -> story.getSummary //TODO: dont even fetch data from database
            //"Summary"   -> story.description
        )
    }

    implicit val userFeedReadStatWrites = new Writes[UserFeedReadStat] {
        def writes(feedStat:UserFeedReadStat) = Json.obj (
            "UserId"    -> feedStat.userId,
            "FeedId"    -> feedStat.feedId,
            "StartFrom" -> feedStat.startFrom.getTime / 1000.0f,
            "UnreadCount" -> feedStat.unreadCount
        )
    }

    implicit val feedWrites = new Writes[Feed] {
        def writes(feed: Feed) = Json.obj(
            "Id"        -> feed.feedId,
            "XmlUrl"    -> feed.xmlUrl
        )
    }

    implicit val opmlOutlineWrites = new Writes[OpmlOutline] {
        def writes(opmlOutline: OpmlOutline) = Json.obj(
            "Title"     -> opmlOutline.title,
            "HtmlUrl"   -> opmlOutline.htmlUrl,
            "XmlUrl"    -> opmlOutline.xmlUrl,
            "Type"      -> opmlOutline.outlineType,
            "Image"     -> "abcdefghijklmn", //TODO: avatar
            "Text"      -> opmlOutline.text
        )
    }

    implicit val opmlWrites = new Writes[Opml] {
        def writes(opml: Opml) = JsArray(
            opml.outlines.foldLeft[List[JsObject]](List[JsObject]())((acc, node) => {
                val subOutlines = node.outlines
                    val subopmllist = subOutlines.foldLeft[List[JsObject]](List[JsObject]())((acc2, node2) => {
                        val nid2 = OpmlOutline2JsObject(List[JsObject](), node2)
                        acc2 :+ nid2
                    })
                val nid = OpmlOutline2JsObject(subopmllist, node)
                acc :+ nid
            })
        )
    }

    def OpmlOutline2JsObject(children: List[JsObject], opmlOutline: OpmlOutline): JsObject = {
        val opmlOutlineJson = Json.toJson(opmlOutline)
        if (children.nonEmpty) {
            opmlOutlineJson.as[JsObject] + ("Outline" -> JsArray(children))
        }
        else {
            opmlOutlineJson.as[JsObject] + ("Outline" -> JsArray())
        }
    }

    def FeedStoryMap2JsObject(mp: Map[String, Iterable[Story]]): JsObject = {
        JsObject(
            mp.map(kv => (
                kv._1,
                JsArray(kv._2.map(Json.toJson(_)).toList)
                )
            ).toSeq
        )
    }


    //Read section
    def JsObject2OpmlOutline(children: List[OpmlOutline], node: JsObject): OpmlOutline = {
        new OpmlOutline(
            children, (node \ "Title").as[String], (node \ "XmlUrl").as[String],
            (node \ "Type").as[String], (node \ "Text").as[String], (node \ "HtmlUrl").as[String]
        )
    }

    def JsObject2OpmlOutlines(opmlJson:List[JsObject]):List[OpmlOutline] = {
        opmlJson.foldLeft[List[OpmlOutline]](List[OpmlOutline]())((acc, node) => {
          val outline2 = (node \ "Outline").as[List[JsObject]]
          val result2 = outline2.foldLeft[List[OpmlOutline]](List[OpmlOutline]())((acc2, node2) => {
            val nid2 = JsObject2OpmlOutline(List[OpmlOutline](), node2)
            acc2 :+ nid2
          })
          val nid = JsObject2OpmlOutline(result2, node)
          acc :+ nid
        })
    }

}
