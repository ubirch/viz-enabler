package com.ubirch.viz

import com.google.inject.{ Guice, Injector, Module }
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.ClassTag

/**
  * Helper to manage Guice Injection.
  */
abstract class InjectorHelper(val modules: List[Module]) extends LazyLogging {

  import InjectorHelper._

  private val injector: Injector = {
    try {
      Guice.createInjector(modules: _*)
    } catch {
      case e: Exception =>
        logger.error("Error Creating Injector: {} ", e.getMessage)
        throw InjectorCreationException(e.getMessage)
    }
  }

  def get[T](implicit ct: ClassTag[T]): T = get(ct.runtimeClass.asInstanceOf[Class[T]])

  def get[T](clazz: Class[T]): T = {
    try {
      injector.getInstance(clazz)
    } catch {
      case e: Exception =>
        logger.error("Error Injecting: {} ", e.getMessage)
        throw InjectionException(e.getMessage)
    }
  }

}

object InjectorHelper {
  /**
    * Represents an Exception for when injecting a component
    * @param message Represents the error message
    */
  case class InjectionException(message: String) extends Exception(message)

  /**
    * Represents an Exception for when creating an injector a component
    * @param message Represents the error message
    */
  case class InjectorCreationException(message: String) extends Exception(message)
}

/**
  * Represents an assembly for the boot process
  */
abstract class Boot(modules: List[Module]) extends InjectorHelper(modules) {
  def *[T](block: => T): Unit =
    try { val _ = block } catch {
      case e: Exception =>
        logger.error("Exiting after exception found = {}", e.getMessage)
        Thread.sleep(5000)
        sys.exit(1)
    }
}
