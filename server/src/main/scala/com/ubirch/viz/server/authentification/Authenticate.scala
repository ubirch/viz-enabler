package com.ubirch.viz.server.authentification

import com.ubirch.viz.server.config.ConfigBase
import com.ubirch.viz.server.models.Elements
import javax.servlet.http.HttpServletRequest
import scalaj.http
import scalaj.http.{ Http, HttpResponse }

object Authenticate extends ConfigBase {
  def isUserAuthorized(incomingRequest: HttpServletRequest): Boolean = {
    val authenticationResponse = createRequestAndGetAuthorizationResponse(incomingRequest)
    isAuthorisationCodeCorrect(authenticationResponse)
  }

  def createRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): http.HttpResponse[String] = {
    val authenticationRequestHeaders = createHeaders(incomingRequest)
    sendAndReceiveRequest(authenticationRequestHeaders)
  }

  def createHeaders(request: HttpServletRequest): Seq[(String, String)] = {
    Seq(
      (Elements.UBIRCH_ID_HEADER, request.getHeader(Elements.UBIRCH_ID_HEADER)),
      (Elements.UBIRCH_PASSWORD_HEADER, request.getHeader(Elements.UBIRCH_PASSWORD_HEADER))
    )
  }

  def sendAndReceiveRequest(headers: Seq[(String, String)]): HttpResponse[String] = {
    Http(ubirchAuthenticationEndpointUrl).headers(headers).asString
  }

  def isAuthorisationCodeCorrect(authenticationResponse: http.HttpResponse[String]): Boolean = {
    authenticationResponse.code.equals(Elements.AUTHORIZATION_SUCCESS_CODE)
  }
}
