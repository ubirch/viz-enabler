package com.ubirch.viz.models

import com.ubirch.viz.models.message.{ MessageTypeOne, MessageTypeZero }
import com.ubirch.viz.models.payload.PayloadJson
import org.scalatest.{ FeatureSpec, Matchers }

import java.util.UUID

class PayloadJsonSpec extends FeatureSpec with Matchers {
  feature("toMessage") {
    scenario("get MessageTypeZero when msg_type is 0") {
      val payload = s"""{"uuid": "${UUID.randomUUID()}", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val payloadJson = new PayloadJson(payload)
      payloadJson.toMessage.isInstanceOf[MessageTypeZero] shouldBe true
    }

    scenario("get MessageTypeOne when msg_type is 1") {
      val payload = s"""{"uuid": "${UUID.randomUUID()}", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 1}"""
      val payloadJson = new PayloadJson(payload)
      payloadJson.toMessage.isInstanceOf[MessageTypeOne] shouldBe true
    }

    scenario("get error when msg_type is unknown") {
      val payload = s"""{"uuid": "${UUID.randomUUID()}", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 2}"""
      val payloadJson = new PayloadJson(payload)
      assertThrows[IllegalArgumentException](payloadJson.toMessage)
    }
  }
}
