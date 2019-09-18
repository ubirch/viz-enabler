package com.ubirch.viz.server.rest

import com.typesafe.scalalogging.LazyLogging
//import com.ubirch.viz.core.elastic.EsClient
import com.ubirch.viz.server.authentification.{AuthenticateDevice, AuthenticationSupport}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport, SwaggerSupportSyntax}
import org.scalatra.{CorsSupport, ScalatraServlet}


class ApiRest(implicit val swagger: Swagger) extends ScalatraServlet
  with NativeJsonSupport with SwaggerSupport with CorsSupport with LazyLogging with AuthenticationSupport {

  // Allows CORS support to display the swagger UI when using the same network
  options("/*") {
    response.setHeader(
      "Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers")
    )
  }

  // Stops the APIJanusController from being abstract
  protected val applicationDescription = "API for sending data to Ubirch"

  // Sets up automatic case class to JSON output serialization
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  val postData: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendData")
      summary "Send data to ES"
      schemes ("http", "https") // Force swagger ui to use http OR https, only need to say it once
      description "Send a UPP that will be stored to ES"
      tags "send"
      parameters (
      bodyParam[String]("UPP").
      description("name of thing"),
      headerParam[String]("X-Ubirch-Hardware-Id").
        description("HardwareId of the device"),
      headerParam[String]("X-Ubirch-Credential").
        description("password of the device")))

  post("/", operation(postData)) {
    if (!AuthenticateDevice.sendAuth(request)) halt(401)
    val UPP = request.body
    //EsClient.storeDeviceData(UPP)
  }

}

