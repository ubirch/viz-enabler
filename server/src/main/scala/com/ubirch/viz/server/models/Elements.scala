package com.ubirch.viz.server.models

import java.math.BigInteger
import java.util.UUID

import org.joda.time.DateTimeZone
import org.json4s.JsonAST
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._

object Elements {
  val UBIRCH_ID_HEADER = "X-Ubirch-Hardware-Id"
  val UBIRCH_PASSWORD_HEADER = "X-Ubirch-Credential"
  val AUTHORIZATION_SUCCESS_CODE = 200
  val NOT_AUTHORIZED_CODE = 401
  val DEFAULT_ERROR_CODE = 400
  val DEVICE_TYPE_PYSENSE = "pysense"
  val DEVICE_TYPE_PYTRACK = "pytrack"
  val DEVICE_TYPE_UNKNOWN = "null"
  val EXIT_ERROR_CODE: Int = -1
  val DEFAULT_MESSAGE_TYPE = 0
  val ACCELEROMETER_NAME = "Acc"
  val ACCELERATION_NAME = "xyz"
  val TEMPERATURE_NAME = "T"
  val LIGHT_NAME = "L"
  val ROLL_NAME = "roll"
  val PITCH_NAME = "pitch"
  val PRESSURE_NAME = "P"
  val HUMIDITY_NAME = "H"
  val VOLTAGE_NAME = "V"
  val TYPE_NAME = "type"
  val AUTHENTICATION_ERROR_NAME = "Authentication"
  val AUTHENTICATION_ERROR_DESCRIPTION = "Authentication against ubirch login failed."
  val MILLISECONDS_IN_SECOND = 1000
  val DEFAULT_TIMEZONE: DateTimeZone = DateTimeZone.UTC
  val UUID_RADIX = 16
  val UUID_MIDDLE = 16
}

abstract class Message {
  def uuid: String
  def msg_type: Int

  def toJson: String

  def isSameUuid(headerUuid: String): Boolean = headerUuid.equals(uuid)
}

case class MessageTypeZero(uuid: String, msg_type: Int, timestamp: Long, data: JsonAST.JValue) extends Message {

  def convertTimestamp: String = {
    new org.joda.time.DateTime(this.timestamp * Elements.MILLISECONDS_IN_SECOND).toDateTime(Elements.DEFAULT_TIMEZONE).toString
  }

  def toJson: String = {
    val json = ("uuid" -> uuidAsString) ~
      ("timestamp" -> convertTimestamp) ~
      ("data" -> render(data))
    compact(render(json))
  }

  def uuidAsString: String = {
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
