package com.ubirch.viz.core

import java.util.concurrent.TimeUnit.MINUTES

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.core.config.ConfigBase
import com.ubirch.viz.core.elastic.EsClient
import org.scalatest.{BeforeAndAfterEach, FeatureSpec, Matchers}
import pl.allegro.tech.embeddedelasticsearch.{EmbeddedElastic, IndexSettings, PopularProperties}

import scala.collection.JavaConverters._

class EsClientSpec extends FeatureSpec with LazyLogging with Matchers with BeforeAndAfterEach with ConfigBase {

  val embeddedElastic: EmbeddedElastic = EmbeddedElastic.builder()
    .withElasticVersion(elasticVersion)
    .withSetting(PopularProperties.HTTP_PORT, elasticPort)
    //.withSetting(PopularProperties.CLUSTER_NAME, "my_cluster")
    .withEsJavaOpts("-Xms128m -Xmx512m")
    .withStartTimeout(4, MINUTES)
    .withCleanInstallationDirectoryOnStop(true)
    .withIndex(elasticIndex, IndexSettings.builder()
      .build())
    .build()
    .start()

  //  override def beforeEach(): Unit = {
  //
  //  }

  feature("send data to es") {
    scenario("json classic") {
      val payload = """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","timestamp":"2019-09-30T11:59:40.000Z","data":{"L_red":97.0,"T":30.0,"AccY":0.01037598,"L_blue":64.0,"AccZ":1.017822,"AccX":-0.02722168,"V":4.772007,"P":99.75,"AccRoll":1.532012,"H":62.32504,"AccPitch":-0.5838608}}"""
      EsClient.storeDeviceData(payload)
      Thread.sleep(1000)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","timestamp":"2019-09-30T11:59:40.000Z","data":{"L_red":97.0,"T":30.0,"AccY":0.01037598,"L_blue":64.0,"AccZ":1.017822,"AccX":-0.02722168,"V":4.772007,"P":99.75,"AccRoll":1.532012,"H":62.32504,"AccPitch":-0.5838608}}"""
    }
  }

  def getAllDocuments = embeddedElastic.fetchAllDocuments().asScala.toList

}
