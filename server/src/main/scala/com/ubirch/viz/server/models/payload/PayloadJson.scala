package com.ubirch.viz.server.models.payload
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.server.models.message.{Message, MessageTypeZero}
import com.ubirch.viz.server.Util.TimeUtil
import org.json4s.{DefaultFormats, JValue}
import org.json4s.JsonAST.JLong
import org.json4s.jackson.JsonMethods.parse

class PayloadJson(payload: String) extends Payload with LazyLogging {

  implicit val formats: DefaultFormats.type = DefaultFormats

  def toMessage: Message = {
    val parsedMessage: JValue = parseMessage
    (parsedMessage \ "msg_type").extract[Int] match {
      case 0 => parsedMessage.extract[MessageTypeZero]
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
          val extractedTimeStamp = value.extract[String]
          TimeUtil.toUtc(extractedTimeStamp)
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
