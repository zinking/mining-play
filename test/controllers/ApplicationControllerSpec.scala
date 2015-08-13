package controllers

import play.api.http.HeaderNames
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.matcher.ShouldMatchers
import play.api.test.WithApplication
import org.junit.runner.RunWith
import play.api.test.PlaySpecification
import play.api.test.FakeRequest
import play.api.Application

@RunWith(classOf[JUnitRunner])
class ApplicationControllerSpec extends PlaySpecification with ShouldMatchers {

  def applicationController(implicit app: Application) = {
      val app2ApplicationController = Application.instanceCache[controllers.ApplicationController]
      app2ApplicationController(app)
  }  
  
  "Access secured index " in new WithApplication{
    val html = applicationController.index()(FakeRequest())
    status(html) must be equalTo OK
    contentType(html).get must equalTo("text/html")
  }
  
}

