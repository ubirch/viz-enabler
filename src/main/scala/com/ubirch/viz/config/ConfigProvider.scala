package com.ubirch.viz.config

import com.typesafe.config.{ Config, ConfigFactory }
import com.ubirch.viz.config.ConfPaths.{ EsPaths, ServerPaths }
import javax.inject.{ Provider, Singleton }

@Singleton
class ConfigProvider extends Provider[Config] with EsPaths with ServerPaths {

  val default: Config = ConfigFactory.load()

  def conf: Config = default

  override def get(): Config = conf

}
