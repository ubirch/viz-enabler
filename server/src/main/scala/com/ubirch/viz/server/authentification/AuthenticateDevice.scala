package com.ubirch.viz.server.authentification

import com.ubirch.viz.server.Models.Elements
import com.ubirch.viz.server.config.ConfigBase
import javax.servlet.http.HttpServletRequest
import scalaj.http
import scalaj.http.Http

object AuthenticateDevice extends ConfigBase {

  def isAuthorized(incomingRequest: HttpServletRequest): Boolean = {
    val authenticationResponse = sendRequestAndGetAuthorizationResponse(incomingRequest)
    isAuthorisationCodeCorrect(authenticationResponse)
  }

  def sendRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): http.HttpResponse[String] = {
    val authenticationRequestHeaders = createHeaders(incomingRequest)
    Http(ubirchAuthenticationEndpointUrl).headers(authenticationRequestHeaders).asString
  }

  def createHeaders(request: HttpServletRequest): Seq[(String, String)] = {
    Seq(
      (Elements.UBIRCH_ID_HEADER, request.getHeader(Elements.UBIRCH_ID_HEADER)),
      (Elements.UBIRCH_PASSWORD_HEADER, request.getHeader(Elements.UBIRCH_PASSWORD_HEADER))
    )
  }

  def isAuthorisationCodeCorrect(authenticationResponse: http.HttpResponse[String]): Boolean = {
    authenticationResponse.code.equals(Elements.AUTHORIZATION_SUCCESS_CODE)
  }

}
