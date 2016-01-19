package test.util.wiki

import jp.gr.java_conf.hangedman.util.wiki.Wiki
import jp.gr.java_conf.hangedman.model._
import org.specs2.mutable.Specification
import play.api.Logger
import play.api.test.{ FakeRequest, FakeApplication }
import play.api.test.Helpers._
import scala.collection.immutable.HashMap
import scala.util.{ Failure, Success, Try }

class WikiSpec extends Specification {

  "Wiki" should {

    val request = FakeRequest(GET, "/")
    val wiki: Wiki = new Wiki("setup.conf", request)

    "Wiki#installPlugin can find plugins in the current classpath" in {
      wiki.installPlugin("amazon").isEmpty
    }

    "Wiki#installPlugin return error when plugin name using \\W words (non alphabet or number)" in {
      wiki.installPlugin("==>").contains("error") equals (true)
      wiki.installPlugin("<*>").contains("error") equals (true)
      wiki.installPlugin("hoge==>").contains("error") equals (true)
    }

    "Wiki#createUrl can create correct URL" in {
      wiki.createUrl(HashMap("action" -> "HOGE", "type" -> "1")) equals ("wiki.cgi?action=HOGE&amp;type=1")
    }

    "Wiki#addHandler can add & call WikiHandler correctly" in {
      import jp.gr.java_conf.hangedman.plugin.todo.ToDoAddHandler
      Try {
        wiki.addHandler("ADD_TODO", new ToDoAddHandler("ADD_TODO", Paragraph, WIKI_FORMAT))
      } match {
        case Success(_) =>
          wiki.callHandler("ADD_TODO") match {
            case Right(result) =>
              result.header.status == OK
            case Left(_) =>
              false
          }
        case Failure(e) =>
          false
      }
    }

    "Wiki#callHandler should return error if un-registered handler called" in {
      import jp.gr.java_conf.hangedman.plugin.todo.ToDoAddHandler
      Try {
        wiki.addHandler("ADD_TODO", new ToDoAddHandler("ADD_TODO", Paragraph, WIKI_FORMAT))
      } match {
        case Success(_) =>
          wiki.callHandler("ADD_XXXX") match {
            case Right(_) =>
              false
            case Left(_) =>
              true
          }
        case Failure(e) =>
          false
      }
    }

  }
}
