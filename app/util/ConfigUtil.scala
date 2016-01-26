package jp.gr.java_conf.hangedman.util

import scala.collection.immutable.HashMap

trait ConfigUtil {

  def initWithDefaultValue(key: String, defaultVal: String)(implicit m: HashMap[String, String]): HashMap[String, String] = {
    if (m.isDefinedAt(key)) {
      m
    } else {
      m.updated(key, defaultVal)
    }
  }

  def initWithDefaultValues(defaultValues: List[(String, String)])(implicit m: HashMap[String, String]): HashMap[String, String] = {
    defaultValues.foldLeft(m) {
      (m, defalutValue) => initWithDefaultValue(defalutValue._1, defalutValue._2)(m)
    }
  }

  def updateWithValue(key: String, value: String)(implicit m: HashMap[String, String]): HashMap[String, String] = {
    m.updated(key, value)
  }

  def updateWithValues(defaultValues: List[(String, String)])(implicit m: HashMap[String, String]): HashMap[String, String] = {
    defaultValues.foldLeft(m) {
      (m, defalutValue) => updateWithValue(defalutValue._1, defalutValue._2)(m)
    }
  }
}
