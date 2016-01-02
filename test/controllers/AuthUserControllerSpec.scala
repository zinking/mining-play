package controllers

import mining.util.DaoTestUtil
import org.specs2.runner.JUnitRunner
import org.specs2.matcher.ShouldMatchers
import play.api.libs.json.Json
import play.api.test.{WithApplication, PlaySpecification, FakeRequest}
import org.junit.runner.RunWith
import play.api.Application
import scala.concurrent.Future
import play.api.mvc.Result

import scala.util.Properties

@RunWith(classOf[JUnitRunner])
class AuthUserControllerSpec extends PlaySpecification with ShouldMatchers {
    Properties.setProp("env", "test")

    def authUserController(implicit app: Application) = {
        val app2ApplicationController = Application.instanceCache[controllers.AuthUserController]
        app2ApplicationController(app)
    }

    val zhangsanEmail = "zhangsan@mining.com"
    val zhangsanPass  = "zhangsan@pass"


    sequential

    "test database tables should be cleaned up" in  new WithApplication{
        DaoTestUtil.truncateAllTables()
    }


    "user should always be possible to access index " in new WithApplication{
        val html = authUserController.loginIndex()(FakeRequest())
        status(html) must be equalTo OK
        contentType(html).get must equalTo("text/html")
    }

    "user should be able to register " in new WithApplication{
        val request = FakeRequest()
            .withHeaders( CONTENT_TYPE -> "application/x-www-form-urlencoded" )
            .withBody((zhangsanEmail,zhangsanPass))
        val registerResult:Future[Result] = authUserController.register()(request)
        status(registerResult) must be equalTo OK
        contentType(registerResult).get must equalTo("text/html")
        val registerResultContent = contentAsString(registerResult)
        registerResultContent must contain ("Done")
    }

    "user cannot register with existing account " in new WithApplication{
        val request = FakeRequest()
            .withHeaders( CONTENT_TYPE -> "application/x-www-form-urlencoded" )
            .withBody((zhangsanEmail,zhangsanPass))
        val registerResult:Future[Result] = authUserController.register()(request)
        status(registerResult) must be equalTo OK
        contentType(registerResult).get must equalTo("text/html")
        val registerResultContent = contentAsString(registerResult)
        registerResultContent must contain ("already been taken")
    }

    "user with invalid credentail should not login " in new WithApplication{
        val request = FakeRequest()
            .withHeaders( CONTENT_TYPE -> "application/x-www-form-urlencoded" )
            .withBody((zhangsanEmail,zhangsanPass+"invalid"))
        val authResult:Future[Result] = authUserController.auth()(request)
        status(authResult) must be equalTo OK
        contentType(authResult).get must equalTo("text/html")
        val registerResultContent = contentAsString(authResult)
        registerResultContent must contain ("Incorrect Credential")
    }

    "user with valid credentail should login " in new WithApplication{
        val request = FakeRequest()
            .withHeaders( CONTENT_TYPE -> "application/x-www-form-urlencoded" )
            .withBody((zhangsanEmail,zhangsanPass))
        val authResult:Future[Result] = authUserController.auth()(request)
        status(authResult) must be equalTo OK
        contentType(authResult).get must equalTo("text/html")
        val registerResultContent = contentAsString(authResult)
        registerResultContent must contain ("Done")
    }

    "user should login via api as well " in new WithApplication{
        val jsonparams = Json.parse(s"""{"email":"$zhangsanEmail","pass":"$zhangsanPass"}""")
        val request = FakeRequest().withJsonBody(jsonparams )
        val authResult:Future[Result] = authUserController.apiAuth()(request)
        status(authResult) must be equalTo OK
        contentType(authResult).get must equalTo("application/json")
        val registerResultContent = contentAsString(authResult)
        registerResultContent must contain ("apiKey")
    }
}