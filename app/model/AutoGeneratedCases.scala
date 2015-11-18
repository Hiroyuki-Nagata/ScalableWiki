package models

// Object MENU contains Set(name)
case class Menu(name: String)
// Object OPTIONAL_PARAMS contains Set(VALUE, NAME)
case class OptionalParams(VALUE: String, NAME: String)
// Object STATUS contains Set(VALUE, NAME)
case class Status(VALUE: String, NAME: String)
// Object CATEGORY contains Set(VALUE, NAME)
case class Category(VALUE: String, NAME: String)
// Object PRIORITY contains Set(VALUE, NAME)
case class Priority(VALUE: String, NAME: String)
// Object SITE_TMPL_THEME contains Set(VALUE)
case class SiteTmplTheme(VALUE: String)
// Object THEME contains Set(VALUE)
case class Theme(VALUE: String)
// Object SITE_WIKI_FORMAT contains Set(VALUE)
case class SiteWikiFormat(VALUE: String)
// Object ACCEPT contains Set(ATTACH_DELETE_0, EDIT_2, USER_REGISTER, ATTACH_DELETE_1, EDIT_0, SHOW_2, ATTACH_DELETE_2, ATTACH_UPDATE_0, EDIT_1, SHOW_0, ATTACH_UPDATE_1, SHOW_1, ATTACH_UPDATE_2)
case class Accept(ATTACH_DELETE_0: String, EDIT_2: String, USER_REGISTER: String, ATTACH_DELETE_1: String, EDIT_0: String, SHOW_2: String, ATTACH_DELETE_2: String, ATTACH_UPDATE_0: String, EDIT_1: String, SHOW_0: String, ATTACH_UPDATE_1: String, SHOW_1: String, ATTACH_UPDATE_2: String)
// Object MAIL contains Set(REMOTE_ADDR, BACKUP_SOURCE, PREFIX, DIFF, USER_AGENT, ID, MODIFIED_SOURCE)
case class Mail(REMOTE_ADDR: String, BACKUP_SOURCE: String, PREFIX: String, DIFF: String, USER_AGENT: String, ID: String, MODIFIED_SOURCE: String)
// Object REFER contains Set(MODE_1, MODE_2, MODE_0)
case class Refer(MODE_1: String, MODE_2: String, MODE_0: String)
// Object ADMIN contains Set(MAIL_PUB, NAME, MAIL)
case class Admin(MAIL_PUB: String, NAME: String, MAIL: String)
