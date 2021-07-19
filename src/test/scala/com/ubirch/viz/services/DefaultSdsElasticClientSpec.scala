package com.ubirch.viz.services

import java.text.SimpleDateFormat
import java.time.{ LocalDate, ZoneOffset }
import java.util.{ Date, Random }

import com.google.inject.binder.ScopedBindingBuilder
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.typesafe.config.{ Config, ConfigValueFactory }
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.{ Binder, InjectorHelper }
import com.ubirch.viz.config.{ ConfigProvider, EsPaths }
import com.ubirch.viz.config.ConfPaths.EsPaths
import com.ubirch.viz.models.{ ElasticResponse, ElasticUtil }
import com.ubirch.viz.models.payload.{ PayloadFactory, PayloadType }
import org.apache.http.auth.{ AuthScope, UsernamePasswordCredentials }
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.HttpHost
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.{ Request, RestClient }
import org.json4s.jackson.JsonMethods._
import org.json4s.{ DefaultFormats, NoTypeHints }
import org.json4s.native.Serialization
import org.scalatest.{ BeforeAndAfterEach, FeatureSpec, Matchers }
import org.testcontainers.elasticsearch.ElasticsearchContainer

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class DefaultSdsElasticClientSpec extends FeatureSpec with LazyLogging with Matchers with BeforeAndAfterEach with EsPaths {

  val DEFAULT_WAIT_TIME = 4000

  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  // Create the elasticsearch container.

  // Start the container. This step might take some time...
  val container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.8.1")
  container.start()

  val actualEsPort: Integer = container.getMappedPort(9200)
  val actualEsHost = container.getHttpHostAddress.split(":").head

  logger.info(s"ES host = $actualEsHost, port = $actualEsPort")

  val Injector: InjectorHelper = FakeSimpleInjector(actualEsHost, actualEsPort)
  val conf = Injector.get[Config]
  // Do whatever you want with the rest client ...
  val credentialsProvider = new BasicCredentialsProvider()
  credentialsProvider.setCredentials(
    AuthScope.ANY,
    new UsernamePasswordCredentials(conf.getString(ES_IO_USER), conf.getString(ES_PASSWORD))
  )

  val esMasterClient: RestClient = RestClient.builder(HttpHost.create(container.getHttpHostAddress))
    .setHttpClientConfigCallback(_.setDefaultCredentialsProvider(credentialsProvider))
    .build()

  esMasterClient.performRequest(new Request("GET", "/_cluster/health"))
  esMasterClient.performRequest(new Request("PUT", s"/${conf.getString(ES_INDEX)}"))

  override def beforeEach(): Unit = {
    purgeEmbeddedEsIndex()
  }

  val defaultUUID = "55424952-3c71-bf88-20dc-3c71bf8820dc"
  val defaultTimestamp = "2019-10-05T07:56:14.187873Z"

  val dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss.SSSZ")

  val esClient: SdsElasticClient = Injector.get[SdsElasticClient]

  feature("send data to es") {
    scenario("json classic") {
      val payload = """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","timestamp":"2019-09-30T11:59:40.000Z","data":{"L_red":97.0,"T":30.0,"AccY":0.01037598,"L_blue":64.0,"AccZ":1.017822,"AccX":-0.02722168,"V":4.772007,"P":99.75,"AccRoll":1.532012,"H":62.32504,"AccPitch":-0.5838608}}"""
      esClient.storeDeviceData(payload)
      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """[{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","timestamp":"2019-09-30T11:59:40.000Z","data":{"L_red":97.0,"T":30.0,"AccY":0.01037598,"L_blue":64.0,"AccZ":1.017822,"AccX":-0.02722168,"V":4.772007,"P":99.75,"AccRoll":1.532012,"H":62.32504,"AccPitch":-0.5838608}}]"""
    }

    scenario("TYPE 0 json payload") {
      val payload = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """[{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-09-30T11:59:40.000Z","data":{"AccZ":1.017822,"H":62.32504,"AccPitch":-0.5838608,"L_red":97,"L_blue":64,"T":30.0,"V":4.772007,"AccX":-0.02722168,"P":99.75,"AccRoll":1.532012,"AccY":0.01037598}}]"""
    }

    scenario("TYPE 0 msgPack payload") {
      val payload = "94c410554249523c71bf8820dc3c71bf8820dc00ce5d932b998ba44163635acb3fefd50000000000a148cb404e26d100000000a84163635069746368cbc026336ea0000000a54c5f726564cd01c7a64c5f626c7565cd0136a154cb403d200000000000a156cb40131bac80000000a441636358cbbfaa800000000000a150cb4070d00000000000a7416363526f6c6ccb4007d3e640000000a441636359cb3fc9040000000000"
      val message = PayloadFactory(payload, PayloadType.MsgPack).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """[{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-10-01T10:34:01.000Z","data":{"L_red":455.0,"T":29.125,"AccY":0.19543457,"L_blue":310.0,"AccZ":0.994751,"AccX":-0.051757812,"V":4.777025,"P":269.0,"AccRoll":2.9784665,"H":60.303253,"AccPitch":-11.100453}}]"""

    }

    scenario("TYPE 0 json payload - timestamp 2") {
      val payload = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val response = getAllDocuments
      response.length shouldBe 1
      response.head shouldBe """[{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-10-05T07:56:14.000Z","data":{"name":"hola","AccZ":1.017822,"H":62.32504,"AccPitch":-0.5838608,"L_red":97,"L_blue":64,"T":30.0,"V":4.772007,"AccX":-0.02722168,"P":99.75,"AccRoll":1.532012,"AccY":0.01037598}}]""".stripMargin
    }
  }

  feature("get a value") {
    scenario("querying from valid uuid with only one value should return it") {
      val payload = {
        s"""{"uuid": "$defaultUUID", "timestamp": "$defaultTimestamp", "data": {"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      }
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastDeviceData(defaultUUID)

      val treatedResFuture = ElasticUtil.parseSingleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      val mapShouldBe: List[(String, String)] = parse("""{"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}""").extract[Map[String, String]].toList.sorted

      val mapIs: List[(String, String)] = treatedRes.data.toList.sorted.map(x => (x._1, String.valueOf(x._2)))

      treatedRes.msg_type shouldBe 0
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
      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc")

      val treatedResFuture = ElasticUtil.parseSingleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      val mapShouldBe = parse("""{"name": "hola5", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}""").extract[Map[String, String]].toList.sorted
      val mapIs: List[(String, String)] = treatedRes.data.toList.sorted.map(x => (x._1, String.valueOf(x._2)))

      mapIs shouldBe mapShouldBe
      treatedRes.uuid shouldBe defaultUUID
    }

    scenario("querying from valid uuid with no values in ES should return empty string") {
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc")

      val treatedResFuture = ElasticUtil.parseSingleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      treatedRes.uuid shouldBe defaultUUID
      treatedRes.timestamp shouldBe None
      treatedRes.data.head._1 shouldBe "errorMessage"
      treatedRes.data.head._2.contains("Error getting value from elasticsearch: ElasticError(search_phase_execution_exception,all shards failed,None,None,None,List(ElasticError(query_shard_exception,No mapping found for [timestamp] in order to sort on") shouldBe true
    }

    scenario("querying from valid uuid with no values should return empty string") {
      val otherUUID = "55424952-3c71-bf88-20dc-3c71bf8820de"
      val payload = s"""{"uuid": "$otherUUID", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      esClient.storeDeviceData(message.toJson)
      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastDeviceData(defaultUUID)

      val treatedResFuture = ElasticUtil.parseSingleData(defaultUUID, res)
      val treadtedRes = Await.result(treatedResFuture, 1.minute)

      treadtedRes shouldBe ElasticResponse(defaultUUID, None, Map("errorMessage" -> "No data associated to the latest record"))
    }

  }

  feature("get values timestamp") {
    scenario("querying from valid uuid with multiple values should return the last one") {
      val payload1 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola1", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload2 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:15.197873Z", "data": {"name": "hola2", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload3 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:16.207873Z", "data": {"name": "hola3", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload4 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:17.217873Z", "data": {"name": "hola4", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payload5 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:18.227873Z", "data": {"name": "hola5", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""

      val messages = PayloadFactory(payload1, PayloadType.Json).toMessage :: PayloadFactory(payload2, PayloadType.Json).toMessage :: PayloadFactory(payload3, PayloadType.Json).toMessage :: PayloadFactory(payload4, PayloadType.Json).toMessage :: PayloadFactory(payload5, PayloadType.Json).toMessage :: Nil

      for (message <- messages) { esClient.storeDeviceData(message.toJson) }

      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getDeviceDataInTimerange("55424952-3c71-bf88-20dc-3c71bf8820dc", "2019-10-05T07:56:13Z", "2019-10-05T07:56:17Z")

      val _ = Await.result(res, 1.minute)

      val treatedResFuture = ElasticUtil.parseMultipleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      treatedRes.responses.size shouldBe 4
    }
  }

  feature("get last n values") {

    val payload1 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola1", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
    val payload2 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:15.197873Z", "data": {"name": "hola2", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
    val payload3 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:16.207873Z", "data": {"name": "hola3", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
    val payload4 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:17.217873Z", "data": {"name": "hola4", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
    val payload5 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:18.227873Z", "data": {"name": "hola5", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""

    scenario("querying from valid uuid with multiple values should return the last one") {

      val messages = PayloadFactory(payload1, PayloadType.Json).toMessage :: PayloadFactory(payload2, PayloadType.Json).toMessage :: PayloadFactory(payload3, PayloadType.Json).toMessage :: PayloadFactory(payload4, PayloadType.Json).toMessage :: PayloadFactory(payload5, PayloadType.Json).toMessage :: Nil

      for (message <- messages) { esClient.storeDeviceData(message.toJson) }

      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastNDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc", 1)

      Await.result(res, 1.minute)

      val treatedResFuture = ElasticUtil.parseMultipleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      treatedRes.responses.size shouldBe 1
      treatedRes.responses.head.timestamp.get shouldBe 1570262178L
    }

    scenario("querying from valid uuid with multiple values should return the last 3 ones") {

      val messages = PayloadFactory(payload1, PayloadType.Json).toMessage :: PayloadFactory(payload2, PayloadType.Json).toMessage :: PayloadFactory(payload3, PayloadType.Json).toMessage :: PayloadFactory(payload4, PayloadType.Json).toMessage :: PayloadFactory(payload5, PayloadType.Json).toMessage :: Nil

      for (message <- messages) { esClient.storeDeviceData(message.toJson) }

      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastNDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc", 3)

      Await.result(res, 1.minute)

      val treatedResFuture = ElasticUtil.parseMultipleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      treatedRes.responses.size shouldBe 3
      treatedRes.responses.map(x => x.timestamp.get).sorted shouldBe List(1570262178L, 1570262177L, 1570262176L).sorted

      // treatedRes.responses.head.timestamp.get.getTime shouldBe 1570262178000L
    }

    scenario("querying from valid uuid with multiple values should return all of them if asking for more") {

      val messages = PayloadFactory(payload1, PayloadType.Json).toMessage :: PayloadFactory(payload2, PayloadType.Json).toMessage :: PayloadFactory(payload3, PayloadType.Json).toMessage :: PayloadFactory(payload4, PayloadType.Json).toMessage :: PayloadFactory(payload5, PayloadType.Json).toMessage :: Nil

      for (message <- messages) { esClient.storeDeviceData(message.toJson) }

      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastNDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc", 10)

      Await.result(res, 1.minute)

      val treatedResFuture = ElasticUtil.parseMultipleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      treatedRes.responses.size shouldBe 5

      // treatedRes.responses.head.timestamp.get.getTime shouldBe 1570262178000L
    }

    scenario("Should only return a maximum of 100") {

      def randomDate: String = {
        val from = LocalDate.of(1000, 3, 1)
        val to = LocalDate.of(2020, 6, 1)
        val diff = java.time.temporal.ChronoUnit.DAYS.between(from, to)
        val random = new Random(System.nanoTime) // You may want a different seed
        val newDate = from.plusDays(random.nextInt(diff.toInt).toLong)
        val date = Date.from(newDate.atStartOfDay(ZoneOffset.UTC).toInstant)
        val strDate = dateFormat.format(date).dropRight(5) + 'Z'
        strDate.replace("-00-", "-03-")
      }

      def generateRandomValues(number: Int) = {
        val res = for (_ <- 0 until number) yield {
          val ts = randomDate
          val accZ = scala.util.Random.nextFloat().toString
          val text = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "$TS", "data": {"name": "hola1", "AccZ": $ACCZ, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
          val res = text.replace("$TS", ts).replace("$ACCZ", accZ)
          res
        }
        res.toSeq
      }

      val valuesToIndex = generateRandomValues(300).map(x => PayloadFactory(x, PayloadType.Json).toMessage).toList

      for (message <- valuesToIndex) { esClient.storeDeviceData(message.toJson) }

      Thread.sleep(DEFAULT_WAIT_TIME.toLong)
      val res: Future[com.sksamuel.elastic4s.Response[SearchResponse]] = esClient.getLastNDeviceData("55424952-3c71-bf88-20dc-3c71bf8820dc", 100)

      Await.result(res, 1.minute)

      val treatedResFuture = ElasticUtil.parseMultipleData(defaultUUID, res)
      val treatedRes = Await.result(treatedResFuture, 1.minute)

      treatedRes.responses.size shouldBe 100

    }
  }

  /**
    * Simple injector that replaces the kafka bootstrap server and topics to the given ones
    */
  def FakeSimpleInjector(host: String, esPort: Integer): InjectorHelper = new InjectorHelper(List(new Binder {
    override def Config: ScopedBindingBuilder = bind(classOf[Config]).toProvider(customTestConfigProvider(host, esPort))
  })) {}

  /**
    * Overwrite default bootstrap server and topic values of the kafka consumer and producers
    */
  def customTestConfigProvider(host: String, port: Int): ConfigProvider = new ConfigProvider {
    override def conf: Config = super.conf.withValue(
      EsPaths.ES_PORT,
      ConfigValueFactory.fromAnyRef(port)
    ).withValue(
        EsPaths.ES_HOST,
        ConfigValueFactory.fromAnyRef(host)
      )
  }

  def getAllDocuments: List[String] = {
    val res = esMasterClient.performRequest(new Request("GET", s"/${conf.getString(ES_INDEX)}/_search"))
    val t = EntityUtils.toString(res.getEntity)
    val p = parse(t) \ "hits" \ "hits" \ "_source"
    import org.json4s.native.Serialization.write
    implicit val formats = Serialization.formats(NoTypeHints)
    val r = write(p)
    List(r)
  }

  def purgeEmbeddedEsIndex(): Unit = {
    esMasterClient.performRequest(new Request("DELETE", s"/${conf.getString(ES_INDEX)}"))
    esMasterClient.performRequest(new Request("PUT", s"/${conf.getString(ES_INDEX)}"))
    ()
  }
}
