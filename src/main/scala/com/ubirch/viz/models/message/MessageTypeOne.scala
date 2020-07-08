package com.ubirch.viz.models.message

import java.math.BigInteger
import java.util.UUID

import com.ubirch.viz.models.Elements
import org.json4s.JsonAST
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, render}

case class MessageTypeOne(uuid: String, msg_type: Int, timestamp: Long, data: JsonAST.JValue, hash: Option[String]) extends Message {

  def toJson: String = {
    val json = ("uuid" -> MessageTypeOne.uuidAsString(uuid)) ~
      ("msg_type" -> msg_type) ~
      ("timestamp" -> MessageTypeOne.convertTimestamp(timestamp)) ~
      ("data" -> render(data)) ~
      ("hash" -> render(hash))
    compact(render(json))
  }
}

object MessageTypeOne {

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
