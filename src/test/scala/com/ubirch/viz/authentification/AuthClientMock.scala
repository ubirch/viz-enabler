package com.ubirch.viz.authentification

import javax.servlet.http.HttpServletRequest
import scalaj.http.HttpResponse

class AuthClientMockAlwaysOk extends AuthClient {

  override def createRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): HttpResponse[String] = {
    HttpResponse(MockValues.MOCK_AUTH_OK_MESSAGE, MockValues.MOCK_AUTH_OK_VALUE, Map.empty)
  }

  override def isAuthorisationCodeCorrect(authenticationResponse: HttpResponse[String]): Boolean = true

  override def isUserAuthorized(incomingRequest: HttpServletRequest): Boolean = true
}

class AuthClientMockAlwaysNok extends AuthClient {

  override def createRequestAndGetAuthorizationResponse(incomingRequest: HttpServletRequest): HttpResponse[String] = {
    HttpResponse(MockValues.MOCK_AUTH_FAIL_MESSAGE, MockValues.MOCK_AUTH_FAIL_VALUE, Map.empty)
  }

  override def isAuthorisationCodeCorrect(authenticationResponse: HttpResponse[String]): Boolean = false

  override def isUserAuthorized(incomingRequest: HttpServletRequest): Boolean = false

}

object MockValues {

  val MOCK_AUTH_FAIL_MESSAGE = "Auth failed"
  val MOCK_AUTH_FAIL_VALUE = 401

  val MOCK_AUTH_OK_MESSAGE = "Ok"
  val MOCK_AUTH_OK_VALUE = 200

}
