package controllers

import org.specs2.runner.JUnitRunner
import org.specs2.matcher.ShouldMatchers
import play.api.test.WithApplication
import org.junit.runner.RunWith
import play.api.test.PlaySpecification
import play.api.test.FakeRequest
import play.api.Application

import scala.util.Properties

@RunWith(classOf[JUnitRunner])
class ApplicationControllerSpec extends PlaySpecification with ShouldMatchers {
  Properties.setProp("env", "test")
  def applicationController(implicit app: Application) = {
      val app2ApplicationController = Application.instanceCache[controllers.ApplicationController]
      app2ApplicationController(app)
  }  
  
  "Access secured page without credential should 404 " in new WithApplication{
    val html = applicationController.index()(FakeRequest())
    status(html) must be equalTo NOT_FOUND
    //contentType(html).get must equalTo("text/html")
  }
}

