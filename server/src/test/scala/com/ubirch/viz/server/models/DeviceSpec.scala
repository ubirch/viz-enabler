package com.ubirch.viz.server.models

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FeatureSpec, Matchers}

class DeviceSpec extends FeatureSpec with LazyLogging with Matchers {

  feature("read message") {
    scenario("deserialze json") {
      val message = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val device = new Device(message)
      val res = device.enrichMessageJson
      println("coucou")
      println(res)

    }

    scenario("deserialize msgpack") {
      val message = "94b0554249523c71bf8820dc3c71bf8820dc00ce5d8b691887a154cb403e000000000000a14c92cce7cd015ea156cb4013168920000000a341636383a378797a93cbbfac000000000000cb3f9b800000000000cb3ff0368000000000a4726f6c6ccb4008b6b9e0000000a57069746368cbbff8414a40000000a150cb4066580000000000a148cb404cc35900000000a474797065a7707973656e7365"
      val device = new Device(message)
      val res = device.enrichMessagePack
      println(res)
    }
  }

}

object truc {
  def main(args: Array[String]): Unit = {
    println("----- MsgPack bellow -------")
    val message = "94c410554249523c71bf8820dc3c71bf8820dc00ce5d91d7938ba44163635acb3ff04e8000000000a148cb404dd1db00000000a84163635069746368cbbfe0af4cc0000000a54c5f726564ccbaa64c5f626c75657ea154cb403ea00000000000a156cb4013168920000000a441636358cbbf98800000000000a150cb4053800000000000a7416363526f6c6ccb3ff58462e0000000a441636359cb3f83000000000000"
    val device = new Device(message)
    val res = device.enrichMessagePack
    println(res)

    println("----- JSON bellow -------")
    val message2 = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
    val device2 = new Device(message2)
    val res2 = device2.enrichMessageJson
    println(res2)
  }
}
