package com.ubirch.viz.server.rest

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.core.{Test, UPP}
import org.json4s.native.Serialization.{read, write}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport, SwaggerSupportSyntax}
import org.scalatra.{CorsSupport, ScalatraServlet}

class ApiRest(implicit val swagger: Swagger) extends ScalatraServlet
  with NativeJsonSupport with SwaggerSupport with CorsSupport with LazyLogging {

  // Allows CORS support to display the swagger UI when using the same network
  options("/*") {
    response.setHeader(
      "Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers")
    )
  }

  // Stops the APIJanusController from being abstract
  protected val applicationDescription = "API for sending data to ubirch"

  // Sets up automatic case class to JSON output serialization
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  val postData: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendData")
      summary "Send data to ES"
      schemes ("http", "https") // Force swagger ui to use http instead of https, only need to say it once TODO: change on prod !!
      description "Send a UPP that will be stored to ES"
      tags "send"
      parameters bodyParam[String]("UPP").
      description("name of thing"))

  post("/", operation(postData)) {
    val UPP = request.body
    write(Test.returnStub(read[UPP](UPP)))
  }

}

