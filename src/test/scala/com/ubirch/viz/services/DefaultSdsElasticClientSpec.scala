package com.ubirch.viz.services

import java.util.concurrent.TimeUnit.MINUTES

import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.{ Binder, InjectorHelper }
import com.ubirch.viz.config.ConfigProvider
import com.ubirch.viz.models.{ ElasticResponse, ElasticUtil }
import com.ubirch.viz.models.payload.{ PayloadFactory, PayloadType }
import org.json4s.jackson.JsonMethods._
import org.scalatest.{ BeforeAndAfterEach, FeatureSpec, Matchers }
import pl.allegro.tech.embeddedelasticsearch.{ EmbeddedElastic, IndexSettings, PopularProperties }

import scala.collection.JavaConverters._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class DefaultSdsElasticClientSpec extends FeatureSpec with LazyLogging with Matchers with BeforeAndAfterEach {

  val DEFAULT_WAIT_TIME = 4000

  val conf = new ConfigProvider

  implicit val formats = org.json4s.DefaultFormats

  val embeddedElastic: EmbeddedElastic = EmbeddedElastic.builder()
    .withElasticVersion(conf.elasticVersion)
    .withSetting(PopularProperties.HTTP_PORT, conf.elasticPort)
    //.withSetting(PopularProperties.CLUSTER_NAME, "my_cluster")
    .withEsJavaOpts("-Xms128m -Xmx512m")
    .withStartTimeout(4, MINUTES)
    .withCleanInstallationDirectoryOnStop(true)
    .withIndex(conf.elasticIndex, IndexSettings.builder()
      .build())
    .build()
    .start()

  override def beforeEach(): Unit = {
    purgeEmbeddedEsIndex()
  }

  val defaultUUID = "55424952-3c71-bf88-20dc-3c71bf8820dc"
  val defaultTimestamp = "2019-10-05T07:56:14.187873Z"

  val Injector = FakeSimpleInjector()
  val esClient = Injector.get[SdsElasticClient]
  feature("send data to es") {
    scenario("json classic") {
      val payload = """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","timestamp":"2019-09-30T11:59:40.000Z","data":{"L_red":97.0,"T":30.0,"AccY":0.01037598,"L_blue":64.0,"AccZ":1.017822,"AccX":-0.02722168,"V":4.772007,"P":99.75,"AccRoll":1.532012,"H":62.32504,"AccPitch":-0.5838608}}"""
      esClient.storeDeviceData(payload)
      Thread.sleep(DEFAULT_WAIT_TIME)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","timestamp":"2019-09-30T11:59:40.000Z","data":{"L_red":97.0,"T":30.0,"AccY":0.01037598,"L_blue":64.0,"AccZ":1.017822,"AccX":-0.02722168,"V":4.772007,"P":99.75,"AccRoll":1.532012,"H":62.32504,"AccPitch":-0.5838608}}"""
    }

    scenario("TYPE 0 json payload") {
      val payload = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-09-30T11:59:40.000Z","data":{"AccZ":1.017822,"H":62.32504,"AccPitch":-0.5838608,"L_red":97,"L_blue":64,"T":30.0,"V":4.772007,"AccX":-0.02722168,"P":99.75,"AccRoll":1.532012,"AccY":0.01037598}}"""
    }

    scenario("TYPE 0 msgPack payload") {
      val payload = "94c410554249523c71bf8820dc3c71bf8820dc00ce5d932b998ba44163635acb3fefd50000000000a148cb404e26d100000000a84163635069746368cbc026336ea0000000a54c5f726564cd01c7a64c5f626c7565cd0136a154cb403d200000000000a156cb40131bac80000000a441636358cbbfaa800000000000a150cb4070d00000000000a7416363526f6c6ccb4007d3e640000000a441636359cb3fc9040000000000"
      val message = PayloadFactory(payload, PayloadType.MsgPack).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-10-01T10:34:01.000Z","data":{"L_red":455.0,"T":29.125,"AccY":0.19543457,"L_blue":310.0,"AccZ":0.994751,"AccX":-0.051757812,"V":4.777025,"P":269.0,"AccRoll":2.9784665,"H":60.303253,"AccPitch":-11.100453}}"""

    }

    scenario("TYPE 0 json payload - timestamp 2") {
      val payload = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-10-05T07:56:14.000Z","data":{"name":"hola","AccZ":1.017822,"H":62.32504,"AccPitch":-0.5838608,"L_red":97,"L_blue":64,"T":30.0,"V":4.772007,"AccX":-0.02722168,"P":99.75,"AccRoll":1.532012,"AccY":0.01037598}}""".stripMargin
    }
  }

  feature("get a value") {
    scenario("querying from valid uuid with only one value should return it") {
      val payload = {
        s"""{"uuid": "$defaultUUID", "timestamp": "$defaultTimestamp", "data": {"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      }
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME)
      val res: Future[Response[SearchResponse]] = esClient.getLastDeviceData(defaultUUID)

      val treatedResFuture = ElasticUtil.parseData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)
      println(treatedRes)

      val mapShouldBe: List[(String, String)] = parse("""{"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}""").extract[Map[String, String]].toList.sorted

      val mapIs: List[(String, String)] = treatedRes.value.toList.sorted.map(x => (x._1, String.valueOf(x._2)))

      mapIs shouldBe mapShouldBe
      treatedRes.uuid shouldBe defaultUUID

    }

    scenario("querying from valid uuid with multiple values should return the last one") {
      val payload1 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola1", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload2 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:15.197873Z", "data": {"name": "hola2", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload3 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:16.207873Z", "data": {"name": "hola3", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload4 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:17.217873Z", "data": {"name": "hola4", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload5 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:18.227873Z", "data": {"name": "hola5", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message1 = PayloadFactory(payload1, PayloadType.Json).toMessage
      val message2 = PayloadFactory(payload2, PayloadType.Json).toMessage
      val message3 = PayloadFactory(payload3, PayloadType.Json).toMessage
      val message4 = PayloadFactory(payload4, PayloadType.Json).toMessage
      val message5 = PayloadFactory(payload5, PayloadType.Json).toMessage
      esClient.storeDeviceData(message1.toJson)
      esClient.storeDeviceData(message2.toJson)
      // putting the last one in the middle to make sure that it's not only
      // the last message put in ES that is being returned
      esClient.storeDeviceData(message5.toJson)
      esClient.storeDeviceData(message3.toJson)
      esClient.storeDeviceData(message4.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME)
      val res: Future[Response[SearchResponse]] = esClient.getLastDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc")

      val treatedResFuture = ElasticUtil.parseData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      val mapShouldBe = parse("""{"name": "hola5", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}""").extract[Map[String, String]].toList.sorted
      val mapIs: List[(String, String)] = treatedRes.value.toList.sorted.map(x => (x._1, String.valueOf(x._2)))

      mapIs shouldBe mapShouldBe
      treatedRes.uuid shouldBe defaultUUID
    }

    scenario("querying from valid uuid with no values in ES should return empty string") {
      val res: Future[Response[SearchResponse]] = esClient.getLastDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc")

      val treatedResFuture = ElasticUtil.parseData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      treatedRes.uuid shouldBe defaultUUID
      treatedRes.ok shouldBe false
      treatedRes.timestamp shouldBe None
      treatedRes.value.head._1 shouldBe "errorMessage"
      treatedRes.value.head._2.contains("Error getting value from elasticsearch: ElasticError(search_phase_execution_exception,all shards failed,None,None,None,List(ElasticError(query_shard_exception,No mapping found for [timestamp] in order to sort on") shouldBe true
    }

    scenario("querying from valid uuid with no values should return empty string") {
      val otherUUID = "55424952-3c71-bf88-20dc-3c71bf8820de"
      val payload = s"""{"uuid": "$otherUUID", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME)
      val res: Future[Response[SearchResponse]] = esClient.getLastDeviceData(defaultUUID)

      val treatedResFuture = ElasticUtil.parseData(defaultUUID, res)
      val treadtedRes = Await.result(treatedResFuture, 1.minute)

      treadtedRes shouldBe ElasticResponse(defaultUUID, ok = false, None, Map("errorMessage" -> "No data associated to the latest record"))
    }

  }

  /**
    * Simple injector that replaces the kafka bootstrap server and topics to the given ones
    */
  def FakeSimpleInjector(): InjectorHelper = new InjectorHelper(List(new Binder)) {}

  def getAllDocuments = embeddedElastic.fetchAllDocuments().asScala.toList

  def purgeEmbeddedEsIndex(): Unit = {
    embeddedElastic.deleteIndex(conf.elasticIndex)
    embeddedElastic.createIndex(conf.elasticIndex)
  }
}
