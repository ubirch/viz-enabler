package com.ubirch.viz.server.authentification

import java.util.Locale

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.server.Models.Device
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.auth.strategy.BasicAuthSupport
import org.scalatra.auth.{ScentryConfig, ScentryStrategy, ScentrySupport}
import org.scalatra.{ScalatraBase, Unauthorized}

import scala.language.implicitConversions

/**
  * Authentication support for routes
  */
trait AuthenticationSupport extends ScentrySupport[Device] with BasicAuthSupport[Device] {
  self: ScalatraBase =>

  protected def fromSession: PartialFunction[String, Device] = {
    case i: String =>
      val splicedString = i.split(";")
      Device(splicedString.head, splicedString(1))
  }

  protected def toSession: PartialFunction[Device, String] = {
    case usr: Device => usr.id
  }

  val realm = "Bearer Authentication"
  protected val scentryConfig: ScentryConfiguration = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]

  override protected def configureScentry: Unit = {
    scentry.unauthenticated {
      scentry.strategies("Bearer").unauthenticated()
    }
  }

  override protected def registerAuthStrategies: Unit = {
    scentry.register("Bearer", app => new BearerStrategy(app, realm))
  }

  // verifies if the request is a Bearer request
  protected def auth()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[Device] = {
    val baReq = new BearerAuthRequest(request)
    if (!baReq.providesAuth) {
      halt(401, "Unauthenticated")
    }
    if (!baReq.isBearerAuth) {
      halt(400, "Bad Request")
    }
    scentry.authenticate("Bearer")
  }

}

class BearerStrategy(protected override val app: ScalatraBase, realm: String) extends ScentryStrategy[Device]
  with LazyLogging {

  implicit def request2BearerAuthRequest(r: HttpServletRequest): BearerAuthRequest = new BearerAuthRequest(r)

  // TODO: remove that
  protected def validate(userName: String, password: String): Option[Device] = {
    None
  }

  protected def getUserId(user: Device): String = user.id

  override def isValid(implicit request: HttpServletRequest): Boolean = request.isBearerAuth && request.providesAuth

  // catches the case that we got none user
  override def unauthenticated()(implicit request: HttpServletRequest, response: HttpServletResponse) {
    app halt Unauthorized()
  }

  // overwrite required authentication request
  def authenticate()(implicit request: HttpServletRequest, response: HttpServletResponse): Option[Device] = validate(request.token)

  protected def validate(token: String): Option[Device] = {
    logger.debug("token: " + token)
    val r = try {
      val opt = Option(Device("a", "b"))
      logger.debug("option token= " + opt.getOrElse("not valid").toString)
      opt
    } catch {
      case e: Throwable => app halt Unauthorized(e.getLocalizedMessage)
    }
    r
  }
}

class BearerAuthRequest(r: HttpServletRequest) {

  private val AUTHORIZATION_KEYS = List("X-Ubirch-Hardware-Id", "X-Ubirch-Credential")

  def parts: List[String] = authorizationKey map {
    r.getHeader(_).split(" ", 2).toList
  } getOrElse Nil


  def scheme: Option[String] = parts.headOption.map(sch => sch.toLowerCase(Locale.ENGLISH))

  def token: String = parts.lastOption getOrElse ""

  private def authorizationKey =AUTHORIZATION_KEYS.find(r.getHeader(_) != null)

  def isBearerAuth: Boolean = (false /: scheme) { (_, sch) => sch == "bearer" }

  def providesAuth: Boolean = authorizationKey.isDefined

}

