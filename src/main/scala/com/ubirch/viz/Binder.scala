package com.ubirch.viz

import com.google.inject.{ AbstractModule, Module }
import com.google.inject.binder.ScopedBindingBuilder
import com.typesafe.config.Config
import com.ubirch.viz.authentification.{ AuthClient, DefaultAuthClient }
import com.ubirch.viz.config.ConfigProvider
import com.ubirch.viz.rest.ApiSwagger
import com.ubirch.viz.services.{ DefaultSdsElasticClient, SdsElasticClient }
import org.scalatra.swagger.Swagger

class Binder extends AbstractModule {

  def ElasticClient: ScopedBindingBuilder = bind(classOf[SdsElasticClient]).to(classOf[DefaultSdsElasticClient])
  def AuthClient: ScopedBindingBuilder = bind(classOf[AuthClient]).to(classOf[DefaultAuthClient])
  def Config: ScopedBindingBuilder = bind(classOf[Config]).toProvider(classOf[ConfigProvider])
  def Swagger: ScopedBindingBuilder = bind(classOf[Swagger]).to(classOf[ApiSwagger])

  def configure(): Unit = {
    ElasticClient
    Config
    AuthClient
    Swagger
  }

}

object Binder {
  def modules: List[Module] = List(new Binder)
}
