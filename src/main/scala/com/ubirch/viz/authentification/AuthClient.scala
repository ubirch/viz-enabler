package com.ubirch.viz.authentification

import com.ubirch.viz.config.ConfigProvider
import com.ubirch.viz.models.Elements
import javax.inject.{ Inject, Singleton }
import javax.servlet.http.HttpServletRequest
import scalaj.http
import scalaj.http.{ Http, HttpResponse }

trait AuthClient {

  def createRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): http.HttpResponse[String]

  def isAuthorisationCodeCorrect(authenticationResponse: http.HttpResponse[String]): Boolean

  def isUserAuthorized(incomingRequest: HttpServletRequest): Boolean

}

@Singleton
class DefaultAuthClient @Inject() (conf: ConfigProvider) extends AuthClient {

  def isUserAuthorized(incomingRequest: HttpServletRequest): Boolean = {
    val authenticationResponse = createRequestAndGetAuthorizationResponse(incomingRequest)
    isAuthorisationCodeCorrect(authenticationResponse)
  }

  def createRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): http.HttpResponse[String] = {
    val authenticationRequestHeaders = createHeaders(incomingRequest)
    sendAndReceiveRequest(authenticationRequestHeaders)
  }

  def isAuthorisationCodeCorrect(authenticationResponse: http.HttpResponse[String]): Boolean = {
    authenticationResponse.code.equals(Elements.AUTHORIZATION_SUCCESS_CODE)
  }

  private def createHeaders(request: HttpServletRequest): Seq[(String, String)] = {
    Seq(
      (Elements.UBIRCH_ID_HEADER, request.getHeader(Elements.UBIRCH_ID_HEADER)),
      (Elements.UBIRCH_PASSWORD_HEADER, request.getHeader(Elements.UBIRCH_PASSWORD_HEADER))
    )
  }

  private def sendAndReceiveRequest(headers: Seq[(String, String)]): HttpResponse[String] = {
    Http(conf.ubirchAuthenticationEndpointUrl).headers(headers).asString
  }
}
