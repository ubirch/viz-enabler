package com.ubirch.viz.server.models

import org.joda.time.DateTimeZone
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonAST
import org.json4s.JsonDSL._

object Elements {
  val UBIRCH_ID_HEADER = "X-Ubirch-Hardware-Id"
  val UBIRCH_PASSWORD_HEADER = "X-Ubirch-Credential"
  val AUTHORIZATION_SUCCESS_CODE = 200
  val AUTHORIZATION_FAIL_CODE = 401
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
  val DEFAULT_TIMEZONE = DateTimeZone.UTC
}

abstract class Message {
  def uuid: String
  def msg_type: Int
  def timestamp: Long
  def data: Map[String, Double]

  def convertTimestamp: String = {
    new org.joda.time.DateTime(this.timestamp*Elements.MILLISECONDS_IN_SECOND).toDateTime(Elements.DEFAULT_TIMEZONE).toString
  }

  def toJson: String = {
    val json = ("uuid" -> uuid) ~
      ("timestamp" -> convertTimestamp) ~
      ("data" -> render(data))
    compact(render(json))
  }
}

case class MessageTypeZero(uuid: String, msg_type: Int, timestamp: Long, data: Map[String, Double]) extends Message

case class MessagePysense(uuid: String, msg_type: Int, timestamp: Long, data: Map[String, Double]) extends Message

//case class MessagePyTrack(uuid: String, msg_type: Int, timestamp: Long, data: DataStructPytrack) extends Message

trait DataStruct {
  def devicetype: String
  def toJValue: JsonAST.JObject
}

//class DataStructPytrack(
//   location: Location,
//   time: String,
//   voltage: Voltage,
//   accelerometer: Accelerometer,
//   devicetype: String
// ) extends DataStruct {
//  override def toJValue: JsonAST.JObject = null
//}

case class DataStructPysense(
    t: Double,
    l: List[Int],
    v: Double,
    acc: NewAccelerometer,
    p: Double,
    h: Double,
    devicetype: String
) extends DataStruct {
  override def toJValue: JsonAST.JObject = {
    ("temperature" -> t) ~
      lightToJValue ~
      ("voltage" -> v) ~
      acc.toJValue ~
      ("pressure" -> p) ~
      ("humidity" -> h)
  }

  def lightToJValue: (String, JsonAST.JObject) = {
    "light" ->
      ("red" -> l.head) ~
      ("blue" -> l(1))
  }

}

trait Measurement {
  def value: Double
  def unit: String
}

case class NewLightSensor(l: List[Int]) {
  def toJValue: (String, JsonAST.JObject) = {
    "light" ->
      ("red" -> l.head) ~
      ("blue" -> l(1))
  }
}

case class NewAccelerometer(xyz: List[Double], roll: Double, pitch: Double) {
  def toJValue: (String, JsonAST.JObject) = {
    "acc" ->
      ("xyz" ->
        ("x" -> xyz.head) ~
        ("y" -> xyz(1)) ~
        ("z" -> xyz(2))) ~
        ("roll" -> roll) ~
        ("pitch" -> pitch)
  }
}

case class Location(longitude: Longitude, latitude: Latitude)

case class Longitude(value: Double, unit: String)
case class Latitude(value: Double, unit: String)

case class Barometer(pressure: Pressure, temperature: Temperature)

case class Pressure(value: Double, unit: String) extends Measurement

case class Temperature(value: Double, unit: String) extends Measurement

case class HumiditySensor(dewpoint: DewPoint, humidity: Humidity, temperature: Temperature)

case class DewPoint(value: Double, unit: String) extends Measurement

case class Humidity(value: Double, unit: String) extends Measurement

case class Voltage(value: Double, unit: String) extends Measurement

case class LightSensor(red: Red, blue: Blue)

case class Red(value: Double, unit: String) extends Measurement

case class Blue(value: Double, unit: String) extends Measurement

case class Accelerometer(xaxis: X_axis, yaxis: Y_axis, zaxis: Z_axis, roll: Roll, pitch: Pitch)

case class X_axis(value: Double, unit: String) extends Measurement

case class Y_axis(value: Double, unit: String) extends Measurement

case class Z_axis(value: Double, unit: String) extends Measurement

case class Roll(value: Double, unit: String) extends Measurement

case class Pitch(value: Double, unit: String) extends Measurement
