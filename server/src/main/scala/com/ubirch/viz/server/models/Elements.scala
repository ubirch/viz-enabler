package com.ubirch.viz.server.models

import org.joda.time.DateTimeZone

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
