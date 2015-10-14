package jp.gr.java_conf.hangedman.controllers

import jp.gr.java_conf.hangedman.model.NormalUser
import jp.gr.java_conf.hangedman.model.Administrator
import jp.t2v.lab.play2.auth.AuthConfig
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results.{ Unauthorized, Forbidden, Redirect }
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scala.reflect.classTag

trait WikiAuthConfig extends AuthConfig {

  override type Id = String
  override type User = jp.gr.java_conf.hangedman.model.User
  override type Authority = jp.gr.java_conf.hangedman.model.Role
  override val idTag: ClassTag[Id] = classTag[Id]
  override def sessionTimeoutInSeconds: Int = 3600 // 1H

  override def resolveUser(id: Id)(implicit context: ExecutionContext): Future[Option[User]] = {
    val r = new NormalUser
    val u = new User("001", "password", r)
    Future.successful(Some(u))
  }
  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.Application.index))
  }
  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.Application.index))
  }
  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Unauthorized("Bad credentials"))
  }
  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden("No permission"))
  }
  override def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean] = Future.successful {
    (user.role, authority) match {
      case (_: Administrator, _) => true // AdminならどんなActionでも全権限を開放
      case (_: NormalUser, _: NormalUser) => true // ユーザがNormalUserで、ActionがNormalUserなら権限あり。もしActionがAdminだけなら権限なしになる。
      case _ => false
    }
  }
  override def authorizationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] =
    throw new AssertionError("don't use")

}
