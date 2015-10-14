package jp.gr.java_conf.hangedman.model

case class User(id: String, pass: String, role: Role)

sealed abstract case class Role()
class Administrator() extends Role()
class NormalUser() extends Role()

case class LoginInfo(id: String, userType: String, path: String)

case class WikiPlugin()
case class Weight()
case class Menu()
