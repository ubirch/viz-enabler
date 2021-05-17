package com.ubirch.viz

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.config.ConfPaths.ServerPaths
import com.ubirch.viz.models.Elements
import javax.inject.Inject
import org.eclipse.jetty.server.{ Handler, HttpConnectionFactory, Server }
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

trait JettyServer {
  def start(): Unit
}

class DefaultJettyServer @Inject() (conf: Config) extends JettyServer with LazyLogging with ServerPaths {

  val serverPort: Int = conf.getInt(SERVER_PORT)
  val serverBaseUrl: String = conf.getString(SERVER_BASE_URL)
  val appVersion: String = conf.getString(APP_VERSION)
  val swaggerPath: String = conf.getString(SERVER_PATH)

  val contextPathBase: String = serverBaseUrl + "/" + appVersion

  def disableServerVersionHeader(server: Server): Unit = {
    server.getConnectors.foreach { connector =>
      connector.getConnectionFactories
        .stream()
        .filter(cf => cf.isInstanceOf[HttpConnectionFactory])
        .forEach(cf => cf.asInstanceOf[HttpConnectionFactory].getHttpConfiguration.setSendServerVersion(false))
    }
  }

  private def initializeServer: Server = {
    val server = createServer
    disableServerVersionHeader(server)
    val contexts = createContextsOfTheServer
    server.setHandler(contexts)
    server
  }

  def start(): Unit = {
    try {
      val server = initializeServer
      server.start()
      server.join()
    } catch {
      case e: Exception =>
        logger.error(e.getMessage)
        System.exit(Elements.EXIT_ERROR_CODE)
    }
  }

  private def createServer = {
    new Server(serverPort)
  }

  private def createContextsOfTheServer = {
    val contextRestApi: WebAppContext = createContextScalatraApi
    val contextSwaggerUi: WebAppContext = createContextSwaggerUi
    initialiseContextHandlerCollection(Array(contextRestApi, contextSwaggerUi))
  }

  private def initialiseContextHandlerCollection(contexts: Array[Handler]): ContextHandlerCollection = {
    val contextCollection = new ContextHandlerCollection()
    contextCollection.setHandlers(contexts)
    contextCollection
  }

  private def createContextScalatraApi: WebAppContext = {
    val contextRestApi = new WebAppContext()
    contextRestApi.setContextPath(contextPathBase)
    contextRestApi.setResourceBase("src/main/scala")
    contextRestApi.addEventListener(new ScalatraListener)
    contextRestApi.addServlet(classOf[DefaultServlet], "/")
    contextRestApi
  }

  private def createContextSwaggerUi: WebAppContext = {
    val contextSwaggerUi = new WebAppContext()
    contextSwaggerUi.setContextPath(contextPathBase + "/docs")
    contextSwaggerUi.setResourceBase(swaggerPath)
    contextSwaggerUi
  }
}
