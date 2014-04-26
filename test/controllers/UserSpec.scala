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
import play.libs.Json

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
    //val Some(jsonresult) = routeAndCall( FakeRequest(POST, "/user/import-opml",FakeHeaders(),data).withCookies(cookie) )
    status(jsonresult) must be equalTo OK
    contentType(jsonresult).get must equalTo("application/json")
  }
  
  "zhangsan should be able to upload opml" in new WithLoggedUser(minimalApp,Some(user1auth) ) {
    val jsonparams = Map( "opml"-> Seq(sampleOpml))
        //headers = FakeHeaders( Seq("))
    val request = FakeRequest( POST, "/user/upload-opml", FakeHeaders(),  jsonparams ).withCookies(cookie)
    val Some(jsonresult ) = route( request )
    //val jsonresult = UserController.uploadOPML()(request)
    //val jsonresult = UserController.uploadOPML()( FakeRequest(POST, "/user/upload-opml", FakeHeaders(), jsonparams ).withCookies(cookie) )
    status(jsonresult) must be equalTo OK
    contentType(jsonresult).get must equalTo("application/json")
  }
}