package com.ubirch.viz.server.models.payload

import com.ubirch.viz.server.models.message.Message

trait Payload {
  def toMessage: Message
}

object PayloadType extends Enumeration {
  type PayloadType = Value
  val MsgPack, Json = Value
}
