package com.ubirch.viz.models

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.models.payload.{ PayloadFactory, PayloadType }
import org.scalatest.{ BeforeAndAfterEach, FeatureSpec, Matchers }

class PayloadMsgPackSpec extends FeatureSpec with LazyLogging with Matchers with BeforeAndAfterEach {

  scenario("TYPE 0 msgPack payload") {
    val payload = "94c410554249523c71bf8820dc3c71bf8820dc00ce5d932b998ba44163635acb3fefd50000000000a148cb404e26d100000000a84163635069746368cbc026336ea0000000a54c5f726564cd01c7a64c5f626c7565cd0136a154cb403d200000000000a156cb40131bac80000000a441636358cbbfaa800000000000a150cb4070d00000000000a7416363526f6c6ccb4007d3e640000000a441636359cb3fc9040000000000"
    val message = PayloadFactory(payload, PayloadType.MsgPack).toMessage
    message.uuid shouldBe "55424952-3c71-bf88-20dc-3c71bf8820dc"
  }

  scenario("TYPE 0 with hash msgPack payload") {
    val payload = "95c4105542495230aea42a5a4830aea42a5a4801ce5dd566098ba44163635acb3ff07b0000000000a148cb403a9e9600000000a84163635069746368cbbfdcad0820000000a54c5f726564ccc9a64c5f626c756546a154cb403ed00000000000a156cb40126cf860000000a441636358cbbf94600000000000a150cb4034800000000000a7416363526f6c6ccb3ff1b4e7a0000000a441636359cb3f80800000000000c4404b0970c56ad5929088463d0855d900ed4ce6be7dd1936e41c4438e3ee4d95cead3f8bf7d0ddab95b65c07302101e936ec79064abeb3063e7ffcc7dc57e30755f"
    val message = PayloadFactory(payload, PayloadType.MsgPack).toMessage
    message.uuid shouldBe "55424952-30ae-a42a-5a48-30aea42a5a48"
    message.hash shouldBe Some("SwlwxWrVkpCIRj0IVdkA7Uzmvn3Rk25BxEOOPuTZXOrT+L99Ddq5W2XAcwIQHpNux5Bkq+swY+f/zH3FfjB1Xw==")
  }

}
