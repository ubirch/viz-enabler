package com.ubirch.viz.server.models

import java.math.BigInteger
import java.util.UUID

import org.json4s.JsonAST
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

abstract class Message {
  val uuid: String
  val msg_type: Int

  def toJson: String
}

case class MessageTypeZero(uuid: String, msg_type: Int, timestamp: Long, data: JsonAST.JValue) extends Message {

  def toJson: String = {
    val json =
      ("uuid" -> MessageTypeZero.uuidAsString(uuid)) ~
        ("timestamp" -> MessageTypeZero.convertTimestamp(timestamp)) ~
        ("data" -> render(data))
    compact(render(json))
  }

}

object MessageTypeZero {

  def convertTimestamp(timestamp: Long): String = {
    new org.joda.time.DateTime(timestamp * Elements.MILLISECONDS_IN_SECOND)
      .toDateTime(Elements.DEFAULT_TIMEZONE)
      .toString
  }

  def uuidAsString(uuid: String): String = {
    try {
      UUID.fromString(uuid).toString
    } catch {
      case _: IllegalArgumentException =>
        new UUID(
          new BigInteger(uuid.substring(0, Elements.UUID_MIDDLE), Elements.UUID_RADIX).longValue(),
          new BigInteger(uuid.substring(Elements.UUID_MIDDLE), Elements.UUID_RADIX).longValue()
        ).toString
    }
  }
}
