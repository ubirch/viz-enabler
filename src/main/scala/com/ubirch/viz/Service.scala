package com.ubirch.viz

import javax.inject.{ Inject, Singleton }

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {
    get[SimpleDataServiceSystem].start()
  }

}

@Singleton
class SimpleDataServiceSystem @Inject() (jettyServer: DefaultJettyServer) {
  def start() = {
    jettyServer.start()
  }
}
