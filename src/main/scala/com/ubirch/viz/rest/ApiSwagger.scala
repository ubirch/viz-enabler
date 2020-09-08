package com.ubirch.viz.rest

import javax.inject.{ Inject, Singleton }
import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ ApiInfo, NativeSwaggerBase, Swagger }

@Singleton
class ResourcesApp @Inject() (val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase

object RestApiInfo extends ApiInfo(
  "Simple Data Service",
  "Docs for the Ubirch Simple Data Service",
  "https://ubirch.de",
  "responsibleperon@ubirch.com",
  "Apache V2",
  "https://www.apache.org/licenses/LICENSE-2.0"
)

@Singleton
class ApiSwagger extends Swagger(Swagger.SpecVersion, "0.1.0", RestApiInfo)
