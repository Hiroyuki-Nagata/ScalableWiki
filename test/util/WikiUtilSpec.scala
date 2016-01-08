package test.util

import jp.gr.java_conf.hangedman.util.WikiUtil
import org.specs2.mutable.Specification
import play.api.test.{ FakeRequest, FakeApplication }
import play.api.test.Helpers._
import scala.collection.immutable.HashMap

class WikiUtilSpec extends Specification {

  "WikiUtil" should {

    "handyphone method should detect handyphones' User-Agent" in {
      val request1 = FakeRequest(GET, "/")
        .withHeaders(("User-Agent", "DoCoMo/2.0 N06A3(c500;TB;W24H16)"))
      WikiUtil.handyphone()(request1).equals(true)
      val request2 = FakeRequest(GET, "/")
        .withHeaders(("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)"))
      WikiUtil.handyphone()(request2).equals(false)
    }

    "smartphone method should detect smartphones' User-Agent" in {
      val request1 = FakeRequest(GET, "/")
        .withHeaders(("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3; ja-jp; SC-02C Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"))
      WikiUtil.smartphone()(request1).equals(true)
      val request2 = FakeRequest(GET, "/")
        .withHeaders(("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)"))
      WikiUtil.smartphone()(request2).equals(false)
    }
  }
}
