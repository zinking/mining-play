package controllers

import play.api.http.HeaderNames
import play.api.mvc.{Request, AnyContent}
import play.api.test.{PlaySpecification, FakeApplication, FakeRequest}
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.matcher.ShouldMatchers
import play.api.test.WithApplication
import org.junit.runner.RunWith
import securesocial.core.IdentityId
import securesocial.core.AuthenticationMethod
import securesocial.testkit.SocialUserGenerator
import securesocial.testkit.WithLoggedUser
import scala.xml.Elem
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import play.api.test.FakeHeaders
import securesocial.core.SocialUser
import play.api.libs.json.Json
import play.api.libs.json.JsArray

@RunWith(classOf[JUnitRunner])
class UserControllerSpec extends PlaySpecification with ShouldMatchers {
  import WithLoggedUser._
  
  def sampleOpml:String = {
    val dom:Elem  = 
	<opml version="1.0">
		<head><title>Sample</title></head>
		<body>
			<outline text="We need more..." title="We need more..." type="rss"
				xmlUrl="http://blog.csdn.net/zhuliting/rss/list" htmlUrl="http://blog.csdn.net/zhuliting"/>
			<outline title="FlexBlogs" text="FlexBlogs">
				<outline text="AdobeAll-Bee" title="AdobeAll-Bee" type="rss"
					xmlUrl="http://www.beedigital.net/blog/?feed=rss2" htmlUrl="http://www.beedigital.net/blog"/>
			</outline>
		</body>
	</opml>
	  
	dom.toString
  }
  
  def minimalApp = FakeApplication(
      withoutPlugins=excludedPlugins,
      additionalPlugins=includedPlugins++List("securesocial.core.DefaultIdGenerator")
  )
  
  val user1auth = SocialUser(IdentityId("zhangsan@gmail.com","google"), "san", "zhang", 
      "zhang san",Some("zhangsan@gmail.com"),None, AuthenticationMethod.OAuth2,None, None, None)
  

  sequential
  "zhangsan should be able to import opml" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
    val tempfile = File.createTempFile("sample", ".opml"); 
    tempfile.deleteOnExit();
    val out = new BufferedWriter(new FileWriter(tempfile));
    out.write( sampleOpml );
    out.close();
    val data = new MultipartFormData(  
        Map(), 
        List(  FilePart("file", "sample.opml", Some("text/xml"), TemporaryFile(tempfile)  ) ), 
        List(),
        List()
    )
    val jsonresult = UserController.importOPML()( FakeRequest(POST, "/user/import-opml",FakeHeaders(),data).withCookies(cookie) )
    status(jsonresult) must be equalTo OK
    contentType(jsonresult).get must equalTo("application/json")
  }

  "zhangsan should be able to export opml" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
    val request = FakeRequest( GET, "/user/export-opml").withCookies( cookie )
    val Some( xmlresult ) = route( request )
    status(xmlresult) must be equalTo OK
    contentType(xmlresult).get must equalTo("text/html")
    contentAsString(xmlresult ) must contain( "http://www.beedigital.net/blog/?feed=rss2" )
  }
  
  
  "zhangsan should be able to add subscription" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
    val request = FakeRequest( POST, "/user/add-subscription")
    		.withHeaders( CONTENT_TYPE -> "application/x-www-form-urlencoded" )
    		.withFormUrlEncodedBody( "url"->"http://coolshell.cn/feed").withCookies( cookie )
    //val Some( jsonresult ) = route( request )
    val jsonresult = UserController.addSubscription()(request)
    status(jsonresult) must be equalTo OK
    contentType(jsonresult).get must equalTo("application/json")
    contentAsString(jsonresult ) must contain( "Subscripton Added" )
  }
  
  "zhangsan should be able to list feeds" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
    val request = FakeRequest( GET, "/user/list-feeds").withCookies( cookie )
    val jsonresult = UserController.listFeeds()(request)
    status(jsonresult) must be equalTo OK
    contentType(jsonresult).get must equalTo("application/json")
    val result = contentAsJson(jsonresult)
    ( result \ "Opml" ).as[JsArray].value.size must be greaterThan( 2 )
    ( result \ "Stories" ).as[JsArray].value.size must be greaterThan( 2 )
    ( result \ "feeds" ).as[JsArray].value.size must be greaterThan( 2 )
  }
  
  "zhangsan should be able to upload opml" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
    val request1 = FakeRequest( GET, "/user/list-feeds").withCookies( cookie )
    val lfresult = UserController.listFeeds()(request1)
    val lfjresult = contentAsJson(lfresult)
    
    val jsonparams = Json.obj( "opml"-> (lfjresult\"Opml").as[JsArray] )
    val request2 = FakeRequest( POST, "/user/upload-opml")
    	.withJsonBody( jsonparams).withHeaders(CONTENT_TYPE->"application/json").withCookies( cookie )
    	
    val jsonresult = UserController.uploadOPML()(request2)
    status(jsonresult) must be equalTo OK
    contentType(jsonresult).get must equalTo("application/json")
  }
  
  "zhangsan should be able to save his preferences" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
       
  }
  
  "zhangsan should be able to star one of the story he read" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
     //star and share action ... can they be treated as the same  
  }
  
  "zhangsan should be able to mark one story he read as read" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
     //there should be different markread [ nature read; markread; markunread ]     
  }
  
  "zhangsan should be able to mark stories of one feed he read as all read" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
     //    
  }
  
  "zhangsan should be able to get a list of stories of a feed" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
     //pagnation should be considered    
  }
  
  "zhangsan should be able to get a list of stared stories of a feed" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
     //pagnation should be considered    
  }
  
  "zhangsan should be able to get a list of story contents of a feed" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
     //pagnation should be considered    
  }  
}