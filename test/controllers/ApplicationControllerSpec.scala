package controllers

import play.api.http.HeaderNames
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification
import org.specs2.matcher.ShouldMatchers
import play.api.test.WithApplication
import org.junit.runner.RunWith
import play.api.test.PlaySpecification
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class ApplicationControllerSpec extends PlaySpecification with ShouldMatchers {
  
  "Access secured index " in {
    val html = controllers.ApplicationController.index()(FakeRequest())
    status(html) must be equalTo OK
    contentType(html).get must equalTo("text/html")
  }
  
}

