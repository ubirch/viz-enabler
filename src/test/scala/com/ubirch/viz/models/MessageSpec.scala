package com.ubirch.viz.models

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.models.payload.{ PayloadFactory, PayloadType }
import org.scalatest.{ FeatureSpec, Matchers }

class MessageSpec extends FeatureSpec with LazyLogging with Matchers {

  feature("msgType0") {
    scenario("deserialze json") {
      val payload = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": 1569844780, "data": {"AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      message.toJson shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-09-30T11:59:40.000Z","data":{"AccZ":1.017822,"H":62.32504,"AccPitch":-0.5838608,"L_red":97,"L_blue":64,"T":30.0,"V":4.772007,"AccX":-0.02722168,"P":99.75,"AccRoll":1.532012,"AccY":0.01037598}}"""
    }

    scenario("deserialize msgpack") {
      val payload = "94c410554249523c71bf8820dc3c71bf8820dc00ce5d932b998ba44163635acb3fefd50000000000a148cb404e26d100000000a84163635069746368cbc026336ea0000000a54c5f726564cd01c7a64c5f626c7565cd0136a154cb403d200000000000a156cb40131bac80000000a441636358cbbfaa800000000000a150cb4070d00000000000a7416363526f6c6ccb4007d3e640000000a441636359cb3fc9040000000000"
      val message = PayloadFactory(payload, PayloadType.MsgPack).toMessage
      message.toJson shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-10-01T10:34:01.000Z","data":{"L_red":455.0,"T":29.125,"AccY":0.19543457,"L_blue":310.0,"AccZ":0.994751,"AccX":-0.051757812,"V":4.777025,"P":269.0,"AccRoll":2.9784665,"H":60.303253,"AccPitch":-11.100453}}"""
    }

    scenario("deserialze json from string time without timezone 2") {
      val payload = """{"uuid": "55424952-3c71-bf88-20dc-3c71bf8820dc", "timestamp": "2019-10-05T07:56:14.187873Z", "data": {"name": "hola", "AccZ": 1.017822, "H": 62.32504, "AccPitch": -0.5838608, "L_red": 97, "L_blue": 64, "T": 30.0, "V": 4.772007, "AccX": -0.02722168, "P": 99.75, "AccRoll": 1.532012, "AccY": 0.01037598}, "msg_type": 0}"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      message.toJson shouldBe """{"uuid":"55424952-3c71-bf88-20dc-3c71bf8820dc","msg_type":0,"timestamp":"2019-10-05T07:56:14.000Z","data":{"name":"hola","AccZ":1.017822,"H":62.32504,"AccPitch":-0.5838608,"L_red":97,"L_blue":64,"T":30.0,"V":4.772007,"AccX":-0.02722168,"P":99.75,"AccRoll":1.532012,"AccY":0.01037598}}""".stripMargin

    }
  }

  feature("msgType1") {
    scenario("json") {
      val payload = """{ "uuid": "ba18ff73-73c4-4bd3-9bb6-bbf5db7bc685", "msg_type": 1, "timestamp": 1570266549, "data": { "mac": "DA:A1:19:51:FA:49", "firstTime": "2019-10-03 08:41:59", "lastTime": "2019-10-03 08:41:59", "power": -85, "packetsCount": 1, "BSSID": "(not associated)", "probedESSIDs": "" } }"""
      val message = PayloadFactory(payload, PayloadType.Json).toMessage
      message.toJson shouldBe """{"uuid":"ba18ff73-73c4-4bd3-9bb6-bbf5db7bc685","msg_type":1,"timestamp":"2019-10-05T09:09:09.000Z","data":{"mac":"DA:A1:19:51:FA:49","firstTime":"2019-10-03 08:41:59","lastTime":"2019-10-03 08:41:59","power":-85,"packetsCount":1,"BSSID":"(not associated)","probedESSIDs":""}}"""
    }
  }

}

