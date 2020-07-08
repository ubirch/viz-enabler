package com.ubirch.viz.models.payload

import com.ubirch.viz.models.payload.PayloadType.PayloadType

object PayloadFactory {
  def apply(payload: String, payloadType: PayloadType): Payload = {
    payloadType match {
      case PayloadType.MsgPack => getMsgPack(payload)
      case PayloadType.Json => getJson(payload)
    }
  }

  private def getMsgPack(payload: String): PayloadMsgPack = new PayloadMsgPack(payload)

  private def getJson(payload: String) = new PayloadJson(payload)

}
