import java.io.File
import scala.collection.immutable.TreeMap
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap

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

/**
    val gen = new GenerateCaseClasses(initFile = false)
    val wmm = new GenerateCaseClasses.WikiMultiMap
    gen.generateCaseClasses(wmm)
  */
  }

  val removeRestOfFirstUnderBar = (name: String) =>
  """(.*?)_.*$""".r
    .replaceAllIn(name, m => m.group(1) + "_")
}
