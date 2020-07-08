package com.ubirch.viz.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigBase {
  private def conf: Config = ConfigFactory.load()

  val serverPort: Int = conf.getInt("server.port")
  val serverBaseUrl: String = conf.getString("server.baseUrl")
  val appVersion: String = conf.getString("app.version")
  val swaggerPath: String = conf.getString("server.swaggerPath")
  val ubirchAuthenticationEndpointUrl: String = conf.getString("ubirch.authentication.endpoint")

  val protocol: String = conf.getString("vizEnabler.elasticSearch.protocol")
  val host: String = conf.getString("vizEnabler.elasticSearch.host")
  val elasticPort: Int = conf.getInt("vizEnabler.elasticSearch.port")
  val password: String = conf.getString("vizEnabler.elasticSearch.io_password")
  val username: String = conf.getString("vizEnabler.elasticSearch.io_user")
  val elasticIndex: String = conf.getString("vizEnabler.elasticSearch.deviceData.index")
  val elasticVersion: String = conf.getString("vizEnabler.elasticSearch.version")
}
