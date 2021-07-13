package com.ubirch.viz.models

import org.joda.time.DateTimeZone

object Elements {
  val UBIRCH_ID_HEADER = "X-Ubirch-Hardware-Id"
  val UBIRCH_PASSWORD_HEADER = "X-Ubirch-Credential"
  val AUTHENTICATION_HEADER = "Authorization"
  val AUTHORIZATION_SUCCESS_CODE = 200
  val NOT_AUTHORIZED_CODE = 401
  val DEFAULT_ERROR_CODE = 400
  val DEVICE_TYPE_PYSENSE = "pysense"
  val DEVICE_TYPE_PYTRACK = "pytrack"
  val DEVICE_TYPE_UNKNOWN = "null"
  val EXIT_ERROR_CODE: Int = -1
  val MESSAGE_TYPE_0 = 0
  val MESSAGE_TYPE_1 = 1
  val AUTHENTICATION_ERROR_NAME = "Authentication"
  val MILLISECONDS_IN_SECOND = 1000
  val DEFAULT_TIMEZONE: DateTimeZone = DateTimeZone.UTC
  val UUID_RADIX = 16
  val UUID_MIDDLE = 16
  val MESSAGE_ERROR_DIFFERENT_UUID = "UUIDs in header and payload are different"
}

