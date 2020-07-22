package com.ubirch.viz.config

object ConfPaths {

  trait EsPaths {
    final val ES_PROTOCOL = "vizEnabler.elasticSearch.protocol"
    final val ES_HOST = "vizEnabler.elasticSearch.host"
    final val ES_PORT = "vizEnabler.elasticSearch.port"
    final val ES_PASSWORD = "vizEnabler.elasticSearch.io_password"
    final val ES_IO_USER = "vizEnabler.elasticSearch.io_user"
    final val ES_INDEX = "vizEnabler.elasticSearch.deviceData.index"
    final val ES_VERSION = "vizEnabler.elasticSearch.version"
  }

  trait ServerPaths {
    final val SERVER_PORT = "server.port"
    final val SERVER_BASE_URL = "server.baseUrl"
    final val APP_VERSION = "app.version"
    final val SERVER_PATH = "server.swaggerPath"
    final val SERVER_AUTH_ENDPOINT = "ubirch.authentication.endpoint"
  }

}
