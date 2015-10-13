package jp.gr.java_conf.hangedman.util

import jp.gr.java_conf.hangedman.model._

abstract class AbstractWiki {

  /*
   * Add user; specify 0 if user is admin, else 1
   */
  def addUser(id: String, pass: String, userType: String)
  def addUser(user: User)
  /*
   * Check user exist or not
   */
  def userIsExists(id: String): Boolean
  def userIsExists(user: User): Boolean
  /*
   * Get login infomation if user is logining
   */
  def getLoginInfo(): Option[LoginInfo]
  /*
   * Check login infomation if user is logining
   */
  def checkLogin(id: String, pass: String, path: String): Option[LoginInfo]

  /*
   * Add editform plugin
   */
  def addEditformPlugin(plugin: WikiPlugin, weight: Weight)
  /*
   * Get editform plugin's output
   */
  def getEditformPlugin(): String
}
