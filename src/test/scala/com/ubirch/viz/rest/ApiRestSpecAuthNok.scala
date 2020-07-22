package com.ubirch.viz.rest

import com.google.inject.binder.ScopedBindingBuilder
import com.ubirch.viz.authentification.{ AuthClient, AuthClientMockAlwaysNok, MockValues }
import com.ubirch.viz.models.Elements
import com.ubirch.viz.services.{ SdsElasticClient, SdsElasticClientMock }
import com.ubirch.viz.{ Binder, InjectorHelper }
import org.scalatest.{ BeforeAndAfterEach, FeatureSpec, Matchers }
import org.scalatra.test.scalatest.ScalatraSuite

// have to spearate it in another class as there's issues with scalatra test when mounting multiple time
// the same servlet in the same test class
class ApiRestSpecAuthNok extends FeatureSpec with Matchers with ScalatraSuite with BeforeAndAfterEach {

  implicit val swagger: ApiSwagger = new ApiSwagger

  val defaultUUID = "55424952-3c71-bf88-20dc-3c71bf8820dc"

  feature("send data auth nok") {

    val Injector = FakeInjectorAuthNOk()
    mount(new ApiRest(Injector.get[SdsElasticClient], new AuthClientMockAlwaysNok), "/authNok")

    scenario("json") {
      val payload = s"""{"uuid": "$defaultUUID", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      post(
        uri = "/authNok/json",
        body = payload.getBytes(),
        headers = Map(Elements.UBIRCH_PASSWORD_HEADER -> "eh", Elements.UBIRCH_ID_HEADER -> defaultUUID)
      ) {
          status shouldBe MockValues.MOCK_AUTH_FAIL_VALUE
          body shouldBe s"""{"error":{"error type":"Authentication","message":"${MockValues.MOCK_AUTH_FAIL_MESSAGE}"}}"""
        }
    }

    scenario("mpack") {
      val payload = s"""{"uuid": "$defaultUUID", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      post(
        uri = "/authNok/msgpack",
        body = payload.getBytes(),
        headers = Map(Elements.UBIRCH_PASSWORD_HEADER -> "eh", Elements.UBIRCH_ID_HEADER -> defaultUUID)
      ) {
          status shouldBe MockValues.MOCK_AUTH_FAIL_VALUE
          body shouldBe s"""{"error":{"error type":"Authentication","message":"${MockValues.MOCK_AUTH_FAIL_MESSAGE}"}}"""
        }
    }

  }

  def FakeInjectorAuthNOk(): InjectorHelper = new InjectorHelper(List(new Binder {
    override def ElasticClient: ScopedBindingBuilder = bind(classOf[SdsElasticClient]).to(classOf[SdsElasticClientMock])
    override def AuthClient: ScopedBindingBuilder = bind(classOf[AuthClient]).to(classOf[AuthClientMockAlwaysNok])
  })) {}

}
