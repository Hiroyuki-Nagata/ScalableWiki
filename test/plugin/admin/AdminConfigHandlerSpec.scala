package jp.gr.java_conf.hangedman.plugin.admin

import jp.gr.java_conf.hangedman.model._
import jp.gr.java_conf.hangedman.util.wiki.Wiki
import org.specs2.mutable.Specification
import play.api.Logger
import play.api.test.{ FakeRequest, FakeApplication }
import play.api.test.Helpers._
import scala.collection.immutable.HashMap

class AdminConfigHandlerSpec extends Specification {

  "AdminConfigHandler" should {

    val request = FakeRequest(GET, "/")
    val wiki: Wiki = new Wiki("setup.conf", request)

    "return config HTML for administrator correctly" in {
      val handler = new AdminConfigHandler("N/A", NonSpecify, FSWiki)
      val result = handler.configForm(wiki)
      result.nonEmpty
      result.contains("管理画面")
    }
  }
}
