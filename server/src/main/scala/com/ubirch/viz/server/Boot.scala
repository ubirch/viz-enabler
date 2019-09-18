package com.ubirch.viz.server

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.server.config.ConfigBase
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object Boot extends ConfigBase with LazyLogging {
  def main(args: Array[String]) {

    val server = new Server(serverPort)

    val baseUrl = serverBaseUrl
    val version = "/" + appVersion

    // context for main scalatra rest API
    val context: WebAppContext = new WebAppContext()
    context.setContextPath(baseUrl + version)
    context.setResourceBase("src/main/scala")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    // context for swagger-ui
    val context2 = new WebAppContext()
    context2.setContextPath(baseUrl + version + "/docs")
    context2.setResourceBase(swaggerPath)

    val contexts = new ContextHandlerCollection()
    contexts.setHandlers(Array(context, context2))
    server.setHandler(contexts)

    try {
      server.start()
      server.join()
    } catch {
      case e: Exception =>
        logger.error(e.getMessage)
        System.exit(-1)
    }

    //    val server = new Server(8081)
    //
    //    // context for main scalatra rest API
    //    val context = new WebAppContext()
    //    context.setContextPath("/")
    //    context.setResourceBase("src/main/scala")
    //    context.addEventListener(new ScalatraListener)
    //    context.addServlet(classOf[DefaultServlet], "/")
    //
    //    // context for swagger-ui
    //    val context2 = new WebAppContext()
    //    context2.setContextPath("/docs")
    //    context2.setResourceBase("server/src/main/swagger-ui")
    //
    //    val contexts = new ContextHandlerCollection()
    //    contexts.setHandlers(Array(context, context2))
    //    server.setHandler(contexts)
    //
    //    try {
    //      server.start()
    //      server.join()
    //    } catch {
    //      case e: Exception =>
    //        logger.error(e.getMessage)
    //        System.exit(-1)
    //    }
  }
}
