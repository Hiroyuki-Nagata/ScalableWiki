package test

import org.specs2.mutable.Specification
import play.api.test.{ FakeRequest, FakeApplication }
import play.api.test.Helpers._

class ApplicationSpec extends Specification {

  "Application" should {

    "Root must return HTML" in {
      running(FakeApplication()) {
        val result = route(FakeRequest(GET, "/"))
        status(result.get) mustEqual OK
        //val text = contentAsString(result.get)
        //println(text)
        //text must not empty
      }
    }.pendingUntilFixed("pending unless implementing installPlugin neatly")

    "/swiki also must return HTML" in {
      running(FakeApplication()) {
        val result = route(FakeRequest(GET, "/swiki"))
        println(result)
        result must not beNone
      }
    }
  }
}
