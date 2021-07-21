package com.ubirch.viz.models.payload

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.models.Elements
import com.ubirch.viz.models.message.{ Message, MessageTypeOne, MessageTypeZero }
import com.ubirch.viz.util.TimeUtil
import org.joda.time.DateTime
import org.json4s.{ DefaultFormats, JValue }
import org.json4s.JsonAST.JLong
import org.json4s.jackson.JsonMethods.parse

import scala.util.Try

class PayloadJson(payload: String) extends Payload with LazyLogging {

  implicit val formats: DefaultFormats.type = DefaultFormats

  def toMessage: Message = {
    val parsedMessage: JValue = parseMessage
    val msgType = Try((parsedMessage \ "msg_type").extract[Int]).getOrElse(throw new IllegalArgumentException("msg_type field not found"))
    msgType match {
      case Elements.MESSAGE_TYPE_0 => parsedMessage.extract[MessageTypeZero]
      case Elements.MESSAGE_TYPE_1 => parsedMessage.extract[MessageTypeOne]
      case _ => throw new IllegalArgumentException("Unexpected message type")
    }
  }

  private def parseMessage: JValue = {
    val parsedMessage = parse(payload)
    reformatFieldNames(parsedMessage)
  }

  private def reformatFieldNames(json: JValue): JValue = json.transformField {
    case (field, value) if field == "type" => ("devicetype", value)
    case (field, value) if field == "timestamp" =>

      val newTimeStamp = try {
        val timeStampUTC = {
          value.extractOpt[Long] match {
            case Some(tsLong) =>
              TimeUtil.toZonedDateTime(new DateTime(tsLong))
            case None =>
              val extrac = value.extract[String]
              TimeUtil.toUtc(extrac)
          }

        }
        val zonedDateTime = TimeUtil.toZonedDateTime(timeStampUTC)
        JLong(zonedDateTime.toEpochSecond)
      } catch {
        case e: Exception =>
          logger.warn("Time was time string but couldn't parse it to ISO DATE TIME. Defaulting to Millis, {}", e.getMessage)
          value
      }

      (field, newTimeStamp)
    case (field, value) => (field, value)
  }

}
