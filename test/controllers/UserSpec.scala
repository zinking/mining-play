package controllers

import java.io.File
import java.nio.file.FileSystems
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import mining.io.ser.SerFeedWriter
import mining.io.ser.SerFeedReader
import mining.io.ser.SerFeedManager

@RunWith(classOf[JUnitRunner])
class UserSpec extends FunSuite 
			      with ShouldMatchers 
			      with BeforeAndAfterAll {

  override def beforeAll = {
    //pass
  }

  test("User should be able to authenticate via google auth") {
	  //....
  }
  
}