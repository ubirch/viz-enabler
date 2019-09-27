package com.ubirch.viz.server.rest

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.core.elastic.EsClient
import com.ubirch.viz.server.authentification.Authenticate
import com.ubirch.viz.server.models.{Device, Elements}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.JsonDSL._
import org.scalatra.{CorsSupport, ScalatraServlet}
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport, SwaggerSupportSyntax}

class ApiRest(implicit val swagger: Swagger) extends ScalatraServlet
  with NativeJsonSupport with SwaggerSupport with CorsSupport with LazyLogging {

  // Allows CORS support to display the swagger UI when using the same network
  options("/*") {
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT")
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
  }

  // Stops the APIJanusController from being abstract
  protected val applicationDescription = "API for sending data to Ubirch"

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
      summary "Send json to ES"
      schemes ("http", "https") // Force swagger ui to use http OR https, only need to say it once
      description "Send a JSON message that will be stored to ES"
      tags "send"
      parameters (
        bodyParam[String]("payload").
        description("Payload to be stored, jsonFormat"),
        hwDeviceIdHeaderSwagger,
        passwordHeaderSwagger
      ))

  post("/json", operation(postDataJson)) {
    val message = getDeviceMessage
    logger.debug(s"post(/json), message = $message")
    stopIfDeviceNotAuthorized()
    val device = new Device(message)
    val messageToStore = device.enrichMessageJson
    EsClient.storeDeviceData(messageToStore)
  }

  val postDataMsgPack: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendMsgPack")
      summary "Send msgPack to ES"
      description "Send a msgPack that will be stored to ES"
      tags "send"
      parameters (
        bodyParam[String]("payload").
        description("Payload to be stored, msgpack format"),
        hwDeviceIdHeaderSwagger,
        passwordHeaderSwagger
      ))

  post("/msgPack", operation(postDataMsgPack)) {
    val message = getDeviceMessage
    logger.debug(s"post(/msgPack), message = $message")
    val device = new Device(message)
    stopIfDeviceNotAuthorized()
    val messageToStore = device.enrichMessagePack
    EsClient.storeDeviceData(messageToStore)
  }

  def stopIfDeviceNotAuthorized(): Unit = {
    logger.debug("checking device auth")

    if (!Authenticate.isUserAuthorized(request)) {
      logger.info("Device not authorized")
      halt(401, createServerError(Elements.AUTHENTICATION_ERROR_NAME, Elements.AUTHENTICATION_ERROR_DESCRIPTION))
    }
  }

  def getDeviceMessage: String = {
    val message = request.body
    stopIfMessageEmpty(message)
    message
  }

  def stopIfMessageEmpty(message: String): Unit = {
    if (message.isEmpty) {
      throw new Exception("Message is empty")
    }
  }

  error {
    case e =>
      logger.error(createServerError("Generic error", e.getMessage))
      halt(Elements.DEFAULT_ERROR_CODE, createServerError("Generic error", e.getMessage))
  }

  def createServerError(errorType: String, message: String): String = {
    val errorMessage = "error" ->
      ("error type" -> errorType) ~
      ("message" -> message)
    pretty(render(errorMessage))
  }

}
