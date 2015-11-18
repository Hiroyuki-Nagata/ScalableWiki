import java.io.File
import scala.collection.immutable.TreeMap
import scala.collection.immutable.ListMap
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import com.google.common.base.CaseFormat._

class ReduceArguments(file: String) {

  val targetFile: String = file

  def reduce() = {

    // Generate Map and sort keys from largest to smallest
    // |......
    // |....
    // |..
    while (getCurrentImportsSize > 20) {
      getCurrentFrequencyValues.head match {
        case (leader, companions) =>
          renameArguments(leader, companions)
      }
    }
  }

  def getCurrentFrequencyValues(): ListMap[String, Map[String, String]] = {

    val args: TreeMap[String, String] = getCurrentImportsTreeMap
    val frequencyKeys: List[String] = getCurrentFrequencyKeys(args)

    ListMap(
      frequencyKeys.map {
        frequency => frequency -> args.filterKeys(key => key.startsWith(frequency))
      }.toSeq.sortBy(_._2.size).reverse:_*
    )
    // See also:
    // http://alvinalexander.com/scala/how-to-sort-map-in-scala-key-value-sortby-sortwith
    //
  }

  def getCurrentFrequencyKeys(args: TreeMap[String, String]): List[String] = {
    args.keySet.map {
      key => removeRestOfFirstUnderBar(key)
    }.toList
  }

  def getCurrentImportsTreeMap(): TreeMap[String, String] = {
    TreeMap(
      scala.io.Source.fromFile(targetFile).getLines.toList.head.split(",").map { s =>
        val str = s.replace("@(", "").replace(")", "")
        str.trim.split(":")(0)->str.trim.split(":")(1)
      }.toMap.toArray:_*
    )
  }

  def getCurrentImportsSize(): Int = getCurrentImportsTreeMap.size

  def renameArguments(leader: String, companions: Map[String, String]) = {
    // leader: "FOO_" to "FOO."
    val instanceName: String = leader.replace("_", "")

    import scala.collection.mutable.HashMap
    import scala.collection.mutable.MultiMap
    import scala.collection.mutable.Set
    type WikiMultiMap = HashMap[String, Set[String]] with MultiMap[String, String]

    val gen = new GenerateCaseClasses(initFile = false)
    val wmm = new HashMap[String, Set[String]] with MultiMap[String, String]

    companions.foreach { e =>
      wmm.addBinding(instanceName, e._1.replace(s"${instanceName}_", ""))
    }

    // Generate the case class
    gen.generateCaseClasses(wmm)
    // Collect already defined instances and their companions
    val currentCaseClasses: HashMap[String, Set[String]] = gen.getCurrentCaseClasses
    val className: String = UPPER_UNDERSCORE.to(UPPER_CAMEL, instanceName)

    //
    // Fix the first import line
    //
    val imports: String = scala.io.Source.fromFile(targetFile).getLines.toList.head.replace("@(", "")
    lazy val sources: List[String] = scala.io.Source.fromFile(targetFile).getLines.toList
    lazy val isListNeeded = (instanceName: String) =>
    sources.exists(line => line.contains(instanceName) && line.contains("@for")): Boolean

    val newImports: List[String] = imports.split(",").map {
      imports => imports.trim.split(":")(0)
    }.toList.map {
      imports => if (imports.startsWith(instanceName)) {
        imports.replaceFirst(s"${instanceName}_", s"${instanceName}.")
      } else {
        imports
      }
    }.toList.filterNot {
      imports => imports.contains(s"${instanceName}.")
    }.map { arg =>

      currentCaseClasses.isDefinedAt(arg) match {
        case true =>
          val objectName = LOWER_UNDERSCORE.to(UPPER_CAMEL, arg)
          if (isListNeeded(arg)) {
            s"${arg}: List[${objectName}]"
          } else {
            s"${arg}: ${objectName}"
          }

        case false =>
          s"${arg}: String"
      }
    }.toList ::: List(s"${instanceName}: ${className}")

    println(s"Arguments reduced template file has ${newImports.size} arguments !")

    //
    // Load lines for sources
    //
    val formatted: List[String] = scala.io.Source.fromFile(targetFile).getLines.toList.tail.map {
      //line => line.replaceAll(s"${instanceName}_", s"${instanceName}.")
      line => replaceSpecifiedInstanceName(line, currentCaseClasses)
    }

    // Revert the file, and write
    val f: File = new File(targetFile)
    f.delete
    f.createNewFile

    printToFile(f) { p =>
      (List(newImports.sorted.mkString("@(", ", ", ")"), "") ++ formatted).foreach {
        line => p.println(line)
      }
    }
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(new java.io.FileOutputStream(f, true))
    try { op(p) } finally { p.close() }
  }

  val removeRestOfFirstUnderBar = (name: String) =>
  """(.*?)_.*$""".r
    .replaceAllIn(name, m => m.group(1) + "_")

  def replaceSpecifiedInstanceName
    (line: String,
      currentCaseClasses: scala.collection.mutable.HashMap[String, scala.collection.mutable.Set[String]]) = {

    if (line.matches(""".*?(?!_)([A-Z0-9]*?)_.*""")) {

      val instanceNames = """.*?(?!_)([A-Z0-9]*?)_.*""".r
      val replaced = line match {
        case instanceNames(instanceName) =>
          if (currentCaseClasses.isDefinedAt(instanceName)) {
            println(s"Captured and already defined--> $instanceName")
            line.replaceAll(s"${instanceName}_", s"${instanceName}.")
          } else {
            line
          }
        case _ =>
          line
      }

      replaced
    } else {
      line
    }
  }
}
