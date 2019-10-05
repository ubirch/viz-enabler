package com.ubirch.viz.server.models.payload
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.server.models.{ Message, MessageTypeZero }
import org.json4s.JsonAST.JLong
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{ DefaultFormats, JValue }

class PayloadJson(payload: String) extends Payload with LazyLogging {

  implicit val formats: DefaultFormats.type = DefaultFormats

  def toMessage: Message = {
    val parsedMessage = parseMessage
    parsedMessage.extract[MessageTypeZero]
  }

  private def parseMessage: JValue = {
    val parsedMessage = parse(payload)
    reformatFieldNames(parsedMessage)
  }

  private def reformatFieldNames(json: JValue): JValue = json.transformField {
    case (field, value) if field == "type" => ("devicetype", value)
    case (field, value) if field == "timestamp" =>

      val nv = try {
        val newValue = {
          val vs = value.extract[String]
          if (vs.last.toString != "Z") vs + "Z"
          else vs
        }
        val f = DateTimeFormatter.ISO_DATE_TIME
        val zdt = ZonedDateTime.parse(newValue, f)
        JLong(zdt.toEpochSecond)
      } catch {
        case e: Exception =>
          logger.warn("Time was time string but couldn't parse it to ISO DATE TIME. Defaulting to Millis, {}", e.getMessage)
          value
      }

      (field, nv)
    case (field, value) => (field, value)
  }
}
