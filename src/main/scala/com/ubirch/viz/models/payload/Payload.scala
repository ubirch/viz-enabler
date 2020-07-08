package com.ubirch.viz.models.payload

import com.ubirch.viz.models.message.Message

trait Payload {
  def toMessage: Message
}

object PayloadType extends Enumeration {
  type PayloadType = Value
  val MsgPack, Json = Value
}
