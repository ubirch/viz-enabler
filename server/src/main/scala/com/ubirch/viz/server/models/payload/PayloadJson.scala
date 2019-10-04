package com.ubirch.viz.server.models.payload
import com.ubirch.viz.server.models.{ Message, MessageTypeZero }
import org.json4s.{ DefaultFormats, JValue }
import org.json4s.jackson.JsonMethods.parse

class PayloadJson(payload: String) extends Payload {

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
    case (field, value) => (field, value)
  }
}
