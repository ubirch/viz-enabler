package com.ubirch.viz.core.config

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigBase {

  private def conf: Config = ConfigFactory.load()

  val protocol: String = conf.getString("vizEnabler.elasticSearch.protocol")
  val host: String = conf.getString("vizEnabler.elasticSearch.host")
  val port: Int = conf.getInt("vizEnabler.elasticSearch.port")
  val password: String = conf.getString("vizEnabler.elasticSearch.io_password")
  val username: String = conf.getString("vizEnabler.elasticSearch.io_user")
  val index: String = conf.getString("vizEnabler.elasticSearch.deviceData.index")

}
