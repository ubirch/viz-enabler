package com.ubirch.viz.server.models.message

import java.math.BigInteger
import java.util.UUID

import com.ubirch.viz.server.models.Elements
import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.JsonAST
import org.json4s.JsonDSL._

case class MessageTypeZero(uuid: String, msg_type: Int, timestamp: Long, data: JsonAST.JValue) extends Message {

  def toJson: String = {
    val json = ("uuid" -> MessageTypeZero.uuidAsString(uuid)) ~
      ("msg_type" -> msg_type) ~
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
