package com.ubirch.viz.server.rest

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.core.elastic.EsClient
import com.ubirch.viz.server.authentification.Authenticate
import com.ubirch.viz.server.models.{ Elements, Message, MessageTypeZero }
import com.ubirch.viz.server.models.payload.{ PayloadFactory, PayloadType }
import com.ubirch.viz.server.models.payload.PayloadType.PayloadType
import org.json4s.{ DefaultFormats, Formats }
import org.json4s.JsonDSL._
import org.scalatra.{ CorsSupport, ScalatraServlet }
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{ Swagger, SwaggerSupport, SwaggerSupportSyntax }

class ApiRest(implicit val swagger: Swagger) extends ScalatraServlet
  with NativeJsonSupport with SwaggerSupport with CorsSupport with LazyLogging {

  // Allows CORS support to display the swagger UI when using the same network
  options("/*") {
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT")
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
  }

  // Stops the APIJanusController from being abstract
  protected val applicationDescription = "Simple Data Service"

  // Sets up automatic case class to JSON output serialization
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  val hwDeviceIdHeaderSwagger: SwaggerSupportSyntax.ParameterBuilder[String] = headerParam[String]("X-Ubirch-Hardware-Id").
    description("HardwareId of the device")
  val passwordHeaderSwagger: SwaggerSupportSyntax.ParameterBuilder[String] = headerParam[String]("X-Ubirch-Credential").
    description("Password of the device, base64 encoded")

  val postDataJson: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendJson")
      summary "Send a JSON"
      schemes ("http", "https") // Force swagger ui to use http OR https, only need to say it once
      description "Send a JSON message to the Ubirch Simple Data Service"
      tags "send"
      parameters (
        bodyParam[MessageTypeZero]("payload").
        description("""Payload to be stored, json format. Should have this format: {"uuid": uuid, "msg_type": 0,"timestamp": EPOCH_SECONDS, "data": Map[String, Double]}"""),
        hwDeviceIdHeaderSwagger,
        passwordHeaderSwagger
      ))

  post("/json", operation(postDataJson)) {
    defaultProcess(PayloadType.Json)
  }

  val postDataMsgPack: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendMsgPack")
      summary "Send a msgPack"
      description "Send a msgPack to the Ubirch Simple Data Service"
      tags "send"
      parameters (
        bodyParam[MessageTypeZero]("payload").
        description("""Payload to be stored, msgpack format. Should have this format: {"uuid": uuid, "msg_type": 0,"timestamp": EPOCH_SECONDS, "data": Map[String, Double]}"""),
        hwDeviceIdHeaderSwagger,
        passwordHeaderSwagger
      ))

  post("/msgPack", operation(postDataMsgPack)) {
    defaultProcess(PayloadType.MsgPack)
  }

  private def defaultProcess(payloadType: PayloadType): Unit = {
    val payload = getDeviceMessage
    logIncomingRoad(s"post(/${payloadType.toString})", s"payload = $payload")
    val message = PayloadFactory(payload, payloadType).toMessage
    checkAuthorization(message)
    sendMessageToElasticSearch(message)
  }

  private def sendMessageToElasticSearch(message: Message): Unit = {
    EsClient.storeDeviceData(message.toJson)
  }

  private def checkAuthorization(message: Message): Unit = {
    stopIfNotAuthorized()
    stopIfUuidDifferent(message)
  }

  private def stopIfNotAuthorized(): Unit = {
    logger.debug("checking device auth")

    val keyCloakAuthenticationResponse = Authenticate.createRequestAndGetAuthorizationResponse(request)
    if (!Authenticate.isAuthorisationCodeCorrect(keyCloakAuthenticationResponse)) {
      logger.info("Device not authorized")
      halt(Elements.NOT_AUTHORIZED_CODE, createServerError(Elements.AUTHENTICATION_ERROR_NAME, keyCloakAuthenticationResponse.body))
    }
  }

  private def stopIfUuidDifferent(message: Message): Unit = {
    if (message.uuid != request.getHeader(Elements.UBIRCH_ID_HEADER)) {
      logger.warn("message.uuid:" + message.uuid)
      logger.warn(s"""{"WARN": "UUIDs in header and payload different"}""")
      halt(Elements.NOT_AUTHORIZED_CODE, createServerError(Elements.AUTHENTICATION_ERROR_NAME, "UUIDs in header and payload are different"))
    }
  }

  private def getDeviceMessage: String = {
    val message = request.body
    stopIfMessageEmpty(message)
    message
  }

  private def stopIfMessageEmpty(message: String): Unit = {
    if (message.isEmpty) {
      throw new Exception("Message is empty")
    }
  }

  private def logIncomingRoad(roadName: String, additionalInfo: String = "") = {
    val loggerMessage = ("road" -> roadName) ~ ("additionalInformation" -> additionalInfo)
    logger.debug(compact(render(loggerMessage)))
  }

  error {
    case e =>
      logger.error(createServerError(e.getClass.toString, e.getMessage))
      halt(Elements.DEFAULT_ERROR_CODE, createServerError("Generic error", e.getMessage))
  }

  def createServerError(errorType: String, message: String): String = {
    val errorMessage = "error" ->
      ("error type" -> errorType) ~
      ("message" -> message.replaceAll(System.lineSeparator, ""))
    compact(render(errorMessage))
  }

}
