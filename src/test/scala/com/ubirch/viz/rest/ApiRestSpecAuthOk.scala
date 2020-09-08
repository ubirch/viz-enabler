package com.ubirch.viz.rest

import com.google.inject.binder.ScopedBindingBuilder
import com.ubirch.viz.{ Binder, InjectorHelper }
import com.ubirch.viz.authentification.{ AuthClient, AuthClientMockAlwaysOk }
import com.ubirch.viz.models.Elements
import com.ubirch.viz.services.{ SdsElasticClient, SdsElasticClientMock }
import org.scalatest.{ BeforeAndAfterEach, FeatureSpec, Matchers }
import org.scalatra.test.scalatest.ScalatraSuite

class ApiRestSpecAuthOk extends FeatureSpec with Matchers with ScalatraSuite with BeforeAndAfterEach {

  implicit val swagger: ApiSwagger = new ApiSwagger

  val defaultUUID = "55424952-3c71-bf88-20dc-3c71bf8820dc"

  val Injector = FakeInjectorAuthOk()
  addServlet(new ApiRest(Injector.get[SdsElasticClient], new AuthClientMockAlwaysOk(), new ApiSwagger), "/authOk")

  feature("send data auth ok") {

    scenario("send stuff correct id json") {
      val payload = s"""{"uuid": "$defaultUUID", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      post(
        uri = "/authOk/json",
        body = payload.getBytes(),
        headers = Map(Elements.UBIRCH_PASSWORD_HEADER -> "", Elements.UBIRCH_ID_HEADER -> defaultUUID)
      ) {
          status shouldBe 200
          body shouldBe ""
        }
    }

    scenario("send stuff correct id mpack") {
      val payload = "94c410554249523c71bf8820dc3c71bf8820dc00ce5d932b998ba44163635acb3fefd50000000000a148cb404e26d100000000a84163635069746368cbc026336ea0000000a54c5f726564cd01c7a64c5f626c7565cd0136a154cb403d200000000000a156cb40131bac80000000a441636358cbbfaa800000000000a150cb4070d00000000000a7416363526f6c6ccb4007d3e640000000a441636359cb3fc9040000000000"
      post(
        uri = "/authOk/msgpack",
        body = payload.getBytes(),
        headers = Map(Elements.UBIRCH_PASSWORD_HEADER -> "", Elements.UBIRCH_ID_HEADER -> defaultUUID)
      ) {
          status shouldBe 200
          body shouldBe ""
        }
    }

    scenario("send stuff not same uuid json") {
      val payload = s"""{"uuid": "$defaultUUID", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      post(
        uri = "/authOk/json",
        body = payload.getBytes(),
        headers = Map(Elements.UBIRCH_PASSWORD_HEADER -> "", Elements.UBIRCH_ID_HEADER -> "55424952-3c71-bf88-20dc-3c71bf8820de")
      ) {
          status shouldBe 401
          body shouldBe s"""{"error":{"error type":"Authentication","message":"${Elements.MESSAGE_ERROR_DIFFERENT_UUID}"}}"""
        }
    }

    scenario("send stuff not same uuid msgpack") {
      val payload = "94c410554249523c71bf8820dc3c71bf8820dc00ce5d932b998ba44163635acb3fefd50000000000a148cb404e26d100000000a84163635069746368cbc026336ea0000000a54c5f726564cd01c7a64c5f626c7565cd0136a154cb403d200000000000a156cb40131bac80000000a441636358cbbfaa800000000000a150cb4070d00000000000a7416363526f6c6ccb4007d3e640000000a441636359cb3fc9040000000000"
      post(
        uri = "/authOk/msgpack",
        body = payload.getBytes(),
        headers = Map(Elements.UBIRCH_PASSWORD_HEADER -> "", Elements.UBIRCH_ID_HEADER -> "55424952-3c71-bf88-20dc-3c71bf8820de")
      ) {
          status shouldBe 401
          body shouldBe s"""{"error":{"error type":"Authentication","message":"${Elements.MESSAGE_ERROR_DIFFERENT_UUID}"}}"""
        }
    }
    stop()
  }

  def FakeInjectorAuthOk(): InjectorHelper = new InjectorHelper(List(new Binder {
    override def ElasticClient: ScopedBindingBuilder = bind(classOf[SdsElasticClient]).to(classOf[SdsElasticClientMock])
    override def AuthClient: ScopedBindingBuilder = bind(classOf[AuthClient]).to(classOf[AuthClientMockAlwaysOk])
  })) {}

  //
  //  val conf = new ConfigProvider
  //
  //  val embeddedElastic: EmbeddedElastic = EmbeddedElastic.builder()
  //    .withElasticVersion(conf.elasticVersion)
  //    .withSetting(PopularProperties.HTTP_PORT, conf.elasticPort)
  //    //.withSetting(PopularProperties.CLUSTER_NAME, "my_cluster")
  //    .withEsJavaOpts("-Xms128m -Xmx512m")
  //    .withStartTimeout(4, MINUTES)
  //    .withCleanInstallationDirectoryOnStop(true)
  //    .withIndex(conf.elasticIndex, IndexSettings.builder()
  //      .build())
  //    .build()
  //    .start()
  //
  //  override def beforeEach(): Unit = {
  //    purgeEmbeddedEsIndex()
  //  }
  //
  //  def getAllDocuments = embeddedElastic.fetchAllDocuments().asScala.toList
  //
  //  def purgeEmbeddedEsIndex(): Unit = {
  //    embeddedElastic.deleteIndex(conf.elasticIndex)
  //    embeddedElastic.createIndex(conf.elasticIndex)
  //  }
}
