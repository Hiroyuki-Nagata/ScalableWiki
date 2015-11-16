import java.io.File
import scala.collection.immutable.TreeMap
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
//    while (getCurrentImportsSize > 20) {
      getCurrentFrequencyValues.head match {
        case (leader, companions) =>
          renameArguments(leader, companions)
      }
//    }
  }

  def getCurrentFrequencyValues(): TreeMap[String, Map[String, String]] = {

    val args: TreeMap[String, String] = getCurrentImportsTreeMap
    val frequencyKeys: List[String] = getCurrentFrequencyKeys(args)

    TreeMap(
      frequencyKeys.map {
        frequency => frequency -> args.filterKeys(key => key.contains(frequency))
      }.toMap.toArray:_* //.sortBy(_._2.size).reverse
    )
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
    val className: String = leader.replace("_", "")

    import scala.collection.mutable.HashMap
    import scala.collection.mutable.MultiMap
    import scala.collection.mutable.Set
    type WikiMultiMap = HashMap[String, Set[String]] with MultiMap[String, String]

    val gen = new GenerateCaseClasses(initFile = false)
    val wmm = new HashMap[String, Set[String]] with MultiMap[String, String]

    companions.foreach { e =>
      wmm.addBinding(className, e._1.replace(s"${className}_", ""))
    }

    // Generate the case class
    gen.generateCaseClasses(wmm)
    // Collect already defined instances and their companions
    val currentCaseClasses: HashMap[String, Set[String]] = gen.getCurrentCaseClasses

    //
    // Fix the first import line
    //
    val imports: String = scala.io.Source.fromFile(targetFile).getLines.toList.head.replace("@(", "")
    val replaceTargets: List[String] = imports.split(",").map {
      imports => imports.trim.split(":")(0)
    }.toList
    val replacedElem: List[String] = replaceTargets.map {
      imports => imports.replaceAll(s"${className}_", s"${className}.")
    }.toList
    val newImports = className :: replacedElem.filterNot {
      imports => imports.contains(s"${className}.")
    }.map {
      arg => if (currentCaseClasses.isDefinedAt(arg)) {
        val objectName = arg.toLowerCase
        println(s"${arg}: ${objectName}")
        s"${arg}: ${objectName}"
      } else {
        s"${arg}: String"
      }
    }.toList.sorted

    //
    // Load lines for sources
    //

    //
    val formatted: List[String] = scala.io.Source.fromFile(targetFile).getLines.toList.tail.map {
      line => println(line); line
    }

    // Revert the file, and write
    val f: File = new File(targetFile)
    f.delete
    f.createNewFile

    printToFile(f) { p =>
      (List(newImports.mkString("@(", ", ", ")"), "") ++ formatted).foreach {
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

  val replaceSpecifiedClassName = (line: String, className: String) =>
  """(${className})_""".r
    .replaceAllIn(line, m => m.group(1) + ".")

}
