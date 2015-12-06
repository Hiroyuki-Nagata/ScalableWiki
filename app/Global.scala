import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueFactory
import java.io.File
import jp.gr.java_conf.hangedman.model._
import net.ceedubs.ficus._
import net.ceedubs.ficus.Ficus.{ booleanValueReader, stringValueReader, optionValueReader, toFicusConfig }
import play.api.Application
import play.Logger
import play.api.GlobalSettings
import play.api.cache.Cache
import scala.collection.JavaConversions._
import play.api.Play.current

object Global extends GlobalSettings {

  // load "setup.conf"
  val config: Config = ConfigFactory.parseFile(new File("conf" + File.separator + "setupfile"))
  val defaultConf: Config = ConfigFactory.parseFile(new File("conf" + File.separator + "config.dat"))

  def config(key: String): Option[String] = {
    (config.as[Option[String]](key), defaultConf.as[Option[String]](key)) match {
      case (Some(l), None) =>
        Some(l)
      case (None, Some(r)) =>
        Some(r)
      case (_, _) =>
        None
    }
  }
  def config(key: String, value: String): Unit = {
    config.withValue(key, ConfigValueFactory.fromAnyRef(value))
  }

  implicit def intToRole(i: Int): Role = {
    i match {
      case 0 => Administrator
      case _ => NormalUser
    }
  }

  /**
   * Jobs for starting
   */
  override def onStart(app: Application) = {
    Logger.info("Server starting...")

    //
    // load user information
    //
    ConfigFactory.parseFile(new File("conf/" + config("userdat_file").getOrElse("user.properties"))) match {
      case config: Config =>
        val users: List[Option[User]] = config.root.map {
          case (id: String, cv: ConfigValue) =>
            cv.render match {
              case property if (property.length > 32) =>
                val hash: String = property.substring(1, 33)
                val role: Role = property.substring(property.length - 2) match {
                  case num if (num.length >= 2) =>
                    num.replaceAll("\"", "").toInt
                  case _ =>
                    NormalUser
                }
                Logger.info(s"Adding ${id} ${role} as wiki user...")
                Some(User(id, hash, role))
              case _ =>
                None
            }
          case _ =>
            None
        }.toList

        // set users in cache
        Cache.set("wiki.users", Users(users.flatMap {
          x => x
        }.toList))

      case _ =>
        Logger.warn("userdat_file not found")
    }
  }
}
