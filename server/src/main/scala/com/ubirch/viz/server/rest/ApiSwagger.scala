package com.ubirch.viz.server.rest

import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ApiInfo, NativeSwaggerBase, Swagger}

class ResourcesApp(implicit val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase

//object RestApiInfo {
//  val Info = ApiInfo(
//      "The Ubirch ID API",
//      "Docs for the Ubirch REST ID API",
//      "http://ubirch.de",
//      "responsibleperon@ubirch.com",
//      "Apache V2",
//      "https://www.apache.org/licenses/LICENSE-2.0"
//  )
//}
object RestApiInfo extends ApiInfo(
  "The Ubirch ID API",
  "Docs for the Ubirch REST ID API",
  "https://ubirch.de",
  "responsibleperon@ubirch.com",
  "Apache V2",
  "https://www.apache.org/licenses/LICENSE-2.0"
)

class ApiSwagger extends Swagger(Swagger.SpecVersion, "0.1.0", RestApiInfo)