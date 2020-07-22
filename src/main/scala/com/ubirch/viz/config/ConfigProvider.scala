package com.ubirch.viz.config

import com.typesafe.config.{ Config, ConfigFactory }
import com.ubirch.viz.config.ConfPaths.{ EsPaths, ServerPaths }
import javax.inject.{ Provider, Singleton }

@Singleton
class ConfigProvider extends Provider[Config] with EsPaths with ServerPaths {

  val default: Config = ConfigFactory.load()

  def conf: Config = default

  override def get(): Config = conf

  val serverPort: Int = conf.getInt(SERVER_PORT)
  val serverBaseUrl: String = conf.getString(SERVER_BASE_URL)
  val appVersion: String = conf.getString(APP_VERSION)
  val swaggerPath: String = conf.getString(SERVER_PATH)
  val ubirchAuthenticationEndpointUrl: String = conf.getString(SERVER_AUTH_ENDPOINT)

  val protocol: String = conf.getString(ES_PROTOCOL)
  val host: String = conf.getString(ES_HOST)
  val elasticPort: Int = conf.getInt(ES_PORT)
  val password: String = conf.getString(ES_PASSWORD)
  val username: String = conf.getString(ES_IO_USER)
  val elasticIndex: String = conf.getString(ES_INDEX)
  val elasticVersion: String = conf.getString(ES_VERSION)

}
