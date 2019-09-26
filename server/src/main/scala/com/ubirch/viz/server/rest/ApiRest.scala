package com.ubirch.viz.server.rest

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.core.elastic.EsClient
import com.ubirch.viz.server.authentification.Authenticate
import com.ubirch.viz.server.models.{Device, Elements}
import org.json4s.{DefaultFormats, Formats}
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

  val postDataJson: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendJson")
      summary "Send json to ES"
      schemes ("http", "https") // Force swagger ui to use http OR https, only need to say it once
      description "Send a JSON message that will be stored to ES"
      tags "send"
      parameters (
        bodyParam[String]("payload").
        description("Payload to be stored, jsonFormat"),
        headerParam[String]("X-Ubirch-Hardware-Id").
        description("HardwareId of the device"),
        headerParam[String]("X-Ubirch-Credential").
        description("Password of the device")
      ))

  post("/json", operation(postDataJson)) {
    logger.info("post(/json)")
    logger.info(s"message: $getDeviceMessage")
    val device = new Device(getDeviceMessage)
    stopIfDeviceNotAuthorized(device)
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
        headerParam[String]("X-Ubirch-Hardware-Id").
        description("HardwareId of the device"),
        headerParam[String]("X-Ubirch-Credential").
        description("Password of the device")
      ))

  post("/msgPack", operation(postDataMsgPack)) {
    logger.info("post(/msgPack)")
    logger.info(s"message: $getDeviceMessage")
    val device = new Device(getDeviceMessage)
    stopIfDeviceNotAuthorized(device)
    val messageToStore = device.enrichMessagePack
    EsClient.storeDeviceData(messageToStore)
  }

  def stopIfDeviceNotAuthorized(deviceMessage: Device): Unit = {
    if (!Authenticate.isUserAuthorized(request)) halt(status = Elements.AUTHORIZATION_FAIL_CODE)
  }

  def getDeviceMessage: String = {
    request.body
  }

}
