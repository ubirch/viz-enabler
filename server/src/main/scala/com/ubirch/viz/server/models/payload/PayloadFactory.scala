package com.ubirch.viz.server.models.payload

import com.ubirch.viz.server.models.payload.PayloadType.PayloadType

object PayloadFactory {
  def apply(payload: String, payloadType: PayloadType): Payload = {
    payloadType match {
      case PayloadType.MsgPack => getMsgPack(payload)
      case PayloadType.Json => getJson(payload)
    }
  }

  def getMsgPack(payload: String): PayloadMsgPack = new PayloadMsgPack(payload)

  def getJson(payload: String) = new PayloadJson(payload)

}
