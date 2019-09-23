package com.ubirch.viz.server.authentification

import com.ubirch.viz.server.Models.Elements
import com.ubirch.viz.server.config.ConfigBase
import javax.servlet.http.HttpServletRequest
import scalaj.http
import scalaj.http.{Http, HttpResponse}

class DeviceMessage(message: String) extends ConfigBase {

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

  def enrichMessage: String = {
    message
  }

  //  def getPayloadHash: String = {
  //    implicit val formats = DefaultFormats
  //    val json4sMessage = parse(message)
  //    val messageAsMap = json4sMessage.extract[Map[String, String]]
  //    val messageAsMapSorted = messageAsMap.
  //  }

}
