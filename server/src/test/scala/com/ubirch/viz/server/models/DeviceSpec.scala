package com.ubirch.viz.server.models

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FeatureSpec, Matchers}

class DeviceSpec extends FeatureSpec with LazyLogging with Matchers {

  feature("read message") {
    scenario("deserialze json") {
      val message = """{ "uuid":"UBIR<q�� �<q�� �", "msg_type":0, "timestamp":1569397234, "data":{ "T":27.375, "L":[ 119, 171 ], "V":4.77702522277832, "Acc":{ "xyz":[ -0.0628662109375, 0.0382080078125, 1.0113525390625 ], "roll":3.556959390640259, "pitch":-2.159391164779663 }, "P":203.0, "H":63.683074951171878, "type":"pysense" } }"""
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
    val message = "94b0554249523c71bf8820dc3c71bf8820dc00ce5d8b8a6c8ba44163635acb3ff03e0000000000a148cb404cbe7700000000a84163635069746368cbbff78cb060000000a54c5f726564cc8ba64c5f626c75655aa154cb403d200000000000a156cb4013168920000000a441636358cbbfacc00000000000a150cb4064a80000000000a7416363526f6c6ccb4009542840000000a441636359cb3f9ac00000000000"
    val device = new Device(message)
    val res = device.enrichMessagePack
    println(res)

    println("----- JSON bellow -------")
    val message2 = """{ "uuid":"UBIR<q�� �<q�� �", "msg_type":0, "timestamp":1569397234, "data":{ "AccZ": 1.01513671875, "H": 57.488006591796878, "AccPitch": -1.4718478918075562, "L_red": 139, "L_blue": 90, "T": 29.125, "V": 4.772007465362549, "AccX": -0.05615234375, "P": 165.25, "AccRoll": 3.1660923957824709, "AccY": 0.026123046875 } }"""
    val device2 = new Device(message2)
    val res2 = device2.enrichMessageJson
    println(res2)
  }
}
