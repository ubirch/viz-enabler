package com.ubirch.viz.server

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.server.config.ConfigBase
import com.ubirch.viz.server.models.Elements
import org.eclipse.jetty.server.{Handler, Server}
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object Boot extends ConfigBase with LazyLogging {

  val contextPathBase: String = serverBaseUrl + "/" + appVersion

  def main(args: Array[String]) {
    val server = initializeServer
    startServer(server)

  }

  private def initializeServer: Server = {
    val server = createServer
    val contexts = createContextsOfTheServer
    server.setHandler(contexts)
    server
  }

  private def startServer(server: Server): Unit = {
    try {
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
