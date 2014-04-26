package controllers

import controllers.Application._
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
import securesocial.core.SocialUser

@RunWith(classOf[JUnitRunner])
class ApplicationControllerSpec extends PlaySpecification with ShouldMatchers {
  import WithLoggedUser._
  
  def minimalApp = FakeApplication(
      withoutPlugins=excludedPlugins,
      additionalPlugins=includedPlugins++List("securesocial.core.DefaultIdGenerator")
  )
  
  val user1auth = SocialUser(IdentityId("zhangsan@gmail.com","google"), "san", "zhang", 
      "zhang san",Some("zhangsan@gmail.com"),None, AuthenticationMethod.OAuth2,None, None, None)
  
  "Access secured index " in new WithLoggedUser(minimalApp,Some(user1auth) ) {
    val req: Request[AnyContent] = FakeRequest().withCookies(cookie)

    val html = Application.index.apply(req)
    status(html) must be equalTo OK
    contentAsString(html) must contain( user1auth.email.get )
    contentType(html).get must equalTo("text/html")
    
  }
  
}

