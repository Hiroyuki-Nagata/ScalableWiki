package test.util.wiki

import jp.gr.java_conf.hangedman.util.wiki.Wiki
import org.specs2.mutable.Specification
import play.api.test.{ FakeRequest, FakeApplication }
import play.api.test.Helpers._

class WikiSpec extends Specification {

  "Wiki" should {

    val request = FakeRequest(GET, "/")

    "class Wiki can create instance" in {
      val wiki: Wiki = new Wiki("setup.conf", request)
      true
    }

    "installPlugin return error when plugin name using \\W words (non alphabet or number)" in {
      val wiki: Wiki = new Wiki("setup.conf", request)
      wiki.installPlugin("==>").contains("error") equals (true)
      wiki.installPlugin("<*>").contains("error") equals (true)
      wiki.installPlugin("hoge==>").contains("error") equals (true)
    }
  }
}

