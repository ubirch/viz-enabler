package com.ubirch.viz.server.models

import com.ubirch.viz.server.models.payload.{PayloadFactory, PayloadType}

class Device(payload: String) {

  def enrichMessageJson: String = {
    val message = payloadToJson
    message.toMessage.toJson
  }

  def enrichMessagePack: String = {
    val messagePacked = payloadToMessagePack
    messagePacked.toMessage.toJson
  }

  private def payloadToMessagePack = PayloadFactory(payload, PayloadType.MsgPack)

  private def payloadToJson = PayloadFactory(payload, PayloadType.Json)

}
