package com.ubirch.viz.rest.concerns

import java.util.Locale

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.api.Claims
import com.ubirch.defaults.TokenApi
import monix.execution.Scheduler.Implicits.global
import org.scalatra.Control

import javax.servlet.http.HttpServletRequest
import scala.concurrent.duration._
import scala.util.Try

class BearerAuthRequest(r: HttpServletRequest) {

  private val AUTHORIZATION_KEYS = List("Authorization", "HTTP_AUTHORIZATION", "X-HTTP_AUTHORIZATION", "X_HTTP_AUTHORIZATION")

  def parts: List[String] = authorizationKey map {
    r.getHeader(_).split(" ", 2).toList
  } getOrElse Nil

  def scheme: Option[String] = parts.headOption.map(sch => sch.toLowerCase(Locale.ENGLISH))

  def token: String = parts.lastOption getOrElse ""

  private def authorizationKey = AUTHORIZATION_KEYS.find(r.getHeader(_) != null)

  def isBearerAuth: Boolean = (false /: scheme) { (_, sch) => sch == "bearer" }

  def providesAuth: Boolean = authorizationKey.isDefined

}

object BearerAuthRequest extends Control with LazyLogging {

  def authSystems(request: HttpServletRequest): Try[Claims] = {

    val baReq: BearerAuthRequest = new BearerAuthRequest(request)
    if (!baReq.providesAuth) {
      logger.info("Auth: Unauthenticated")
      halt(401, "Unauthenticated")
    }
    if (!baReq.isBearerAuth) {
      logger.info("Auth: Bad Request")
      halt(400, "Bad Request")
    }
    val res = TokenApi.getClaims(baReq.parts.mkString(" "))
    res
  }

  def verifyUbirchToken(request: HttpServletRequest)(timeout: Duration): Try[Boolean] = {
    BearerAuthRequest.authSystems(request).flatMap { claims =>
      claims.targetIdentities match {
        case Left(List(deviceId)) =>
          TokenApi.externalStateVerifySync(claims.token, deviceId)(timeout).toTry
        case Right(_) => halt(400, "Bad Request")
      }
    }
  }

}
