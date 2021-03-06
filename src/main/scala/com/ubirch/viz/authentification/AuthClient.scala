package com.ubirch.viz.authentification

import com.typesafe.config.Config
import com.ubirch.viz.config.ConfPaths.ServerPaths
import com.ubirch.viz.models.Elements
import com.ubirch.viz.rest.concerns.BearerAuthRequest
import scalaj.http
import scalaj.http.{ Http, HttpResponse }

import javax.inject.{ Inject, Singleton }
import javax.servlet.http.HttpServletRequest
import scala.concurrent.duration._
import scala.util.Try

trait AuthClient {

  def createRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): http.HttpResponse[String]

  def isAuthorisationCodeCorrect(authenticationResponse: http.HttpResponse[String]): Boolean

  def isUserAuthorized(incomingRequest: HttpServletRequest): Boolean

  def fromUbirchToken(incomingRequest: HttpServletRequest): Try[Boolean]

}

@Singleton
class DefaultAuthClient @Inject() (conf: Config) extends AuthClient with ServerPaths {

  override def isUserAuthorized(incomingRequest: HttpServletRequest): Boolean = {
    val authenticationResponse = createRequestAndGetAuthorizationResponse(incomingRequest)
    isAuthorisationCodeCorrect(authenticationResponse)
  }

  override def createRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): http.HttpResponse[String] = {
    val authenticationRequestHeaders = createHeaders(incomingRequest)
    sendAndReceiveRequest(authenticationRequestHeaders)
  }

  override def isAuthorisationCodeCorrect(authenticationResponse: http.HttpResponse[String]): Boolean = {
    authenticationResponse.code.equals(Elements.AUTHORIZATION_SUCCESS_CODE)
  }

  override def fromUbirchToken(incomingRequest: HttpServletRequest): Try[Boolean] = {
    BearerAuthRequest.verifyUbirchToken(incomingRequest)(10.seconds)
  }

  private def createHeaders(request: HttpServletRequest): Seq[(String, String)] = {
    Seq(
      (Elements.UBIRCH_ID_HEADER, request.getHeader(Elements.UBIRCH_ID_HEADER)),
      (Elements.UBIRCH_PASSWORD_HEADER, request.getHeader(Elements.UBIRCH_PASSWORD_HEADER))
    )
  }

  private def sendAndReceiveRequest(headers: Seq[(String, String)]): HttpResponse[String] = {
    Http(conf.getString(SERVER_AUTH_ENDPOINT)).headers(headers).asString
  }
}
