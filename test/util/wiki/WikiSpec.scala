package test.util.wiki

import jp.gr.java_conf.hangedman.util.wiki.Wiki
import org.specs2.mutable.Specification
import play.api.Logger
import play.api.test.{ FakeRequest, FakeApplication }
import play.api.test.Helpers._
import scala.collection.immutable.HashMap

class WikiSpec extends Specification {

  "Wiki" should {

    val request = FakeRequest(GET, "/")
    val wiki: Wiki = new Wiki("setup.conf", request)

    "Wiki#installPlugin can find plugins in the current classpath" in {
      val res = wiki.installPlugin("access")
      println(s"installed => $res")
      true
    }

    "Wiki#installPlugin return error when plugin name using \\W words (non alphabet or number)" in {
      wiki.installPlugin("==>").contains("error") equals (true)
      wiki.installPlugin("<*>").contains("error") equals (true)
      wiki.installPlugin("hoge==>").contains("error") equals (true)
    }

    "Wiki can create correct URL" in {
      wiki.createUrl(HashMap("action" -> "HOGE", "type" -> "1")) equals ("wiki.cgi?action=HOGE&amp;type=1")
    }
  }
}
