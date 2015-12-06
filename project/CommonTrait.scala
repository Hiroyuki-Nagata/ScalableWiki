import com.google.common.base.CaseFormat._
import java.io.File

trait CommonTrait {

  def toCamel(line: String): String = LOWER_UNDERSCORE.to(LOWER_CAMEL, line)

  def toScalaFunc(line: String): String = line.replace("sub", "def").replace(" {", "(): String = {")

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}
