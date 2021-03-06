package com.ubirch.viz.util

import java.time.ZonedDateTime

import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.format.ISODateTimeFormat

object TimeUtil {

  val ZULU_TIME_CHAR = "Z"

  def toUtc(time: String): String = {
    if (!isUtcTime(time)) {
      addUtcTimestamp(time)
    } else {
      time
    }
  }

  def toZonedDateTime(time: String): ZonedDateTime = {
    import java.time.format.DateTimeFormatter

    val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    ZonedDateTime.parse(time, dateTimeFormatter)
  }

  def toZonedDateTime(time: DateTime): String = {
    time.toDateTime(DateTimeZone.UTC).toString(ISODateTimeFormat.basicDateTime())
  }

  private def addUtcTimestamp(time: String) = time + ZULU_TIME_CHAR
  private def isUtcTime(time: String) = time.last.toString == ZULU_TIME_CHAR
}
