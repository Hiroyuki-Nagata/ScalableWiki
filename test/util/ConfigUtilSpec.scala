package test.util

import jp.gr.java_conf.hangedman.util.ConfigUtil
import org.specs2.mutable.Specification
import play.api.test.{ FakeRequest, FakeApplication }
import play.api.test.Helpers._
import scala.collection.immutable.HashMap

class ConfigUtilSpec extends Specification with ConfigUtil {

  "ConfigUtil" should {

    "Update values by specified defalut values" in {

      val m1 = new HashMap[String, String]().empty
      val r1 = initWithDefaultValue("key", "defalut")(m1)
      r1.isDefinedAt("key") equals (true)

      val m2 = HashMap("key1" -> "empty", "key2" -> "emptee")
      val r2 = initWithDefaultValue("key1", "boo")(m2)
      r2.isDefinedAt("key1") equals (true)
      r2("key1") equals ("empty")
    }

    "Update values by specified defalut values with List[_]" in {

      val m1 = new HashMap[String, String]().empty
      val li = List(("key1", "val1"), ("key2", "val2"), ("key3", "val3"))
      val r1 = initWithDefaultValues(li)(m1)
      r1.isDefinedAt("key1") equals (true)
      r1.isDefinedAt("key2") equals (true)
      r1.isDefinedAt("key3") equals (true)
    }
  }
}
