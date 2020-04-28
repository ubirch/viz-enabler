package com.ubirch.viz.server.models.payload

import java.util.Base64

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.server.models.Elements
import com.ubirch.viz.server.models.message.{ Message, MessageTypeOne, MessageTypeZero }
import org.apache.commons.codec.binary.Hex
import org.json4s.{ DefaultFormats, Extraction }
import org.msgpack.core.{ MessageFormat, MessagePack, MessageUnpacker }
import org.msgpack.value.ValueType

class PayloadMsgPack(payload: String) extends Payload with LazyLogging {

  val unpacker: MessageUnpacker = getUnpacker

  private def getUnpacker: MessageUnpacker = {
    val messageBytes = hexToByte(payload)
    MessagePack.newDefaultUnpacker(messageBytes)
  }

  private def hexToByte(hex: String): Array[Byte] = {
    Hex.decodeHex(hex)
  }

  def toMessage: Message = {

    unpacker.unpackArrayHeader()

    val uuid = getUUID
    val msgType = unpackNextAsInt
    extractDependingOnMessageType(uuid, msgType)
  }

  private def getUUID: String = {
    val uuidRaw = unpacker.unpackValue()
    uuidRaw.getValueType match {
      case ValueType.STRING =>
        logger.info("Getting UUID as String")
        uuidRaw.asStringValue().toString
      case _ =>
        logger.info("Getting UUID as Binary")
        val uuidBinary = uuidRaw.asBinaryValue().asByteArray()
        val uuid = MessageTypeZero.uuidAsString(Hex.encodeHexString(uuidBinary))
        uuid
    }
  }

  private def extractDependingOnMessageType(uuid: String, msgType: Int): Message = {
    msgType match {
      case code: Any if code.equals(Elements.MESSAGE_TYPE_0) => typeZeroExtractionStrategy(uuid, msgType)
      case code: Any if code.equals(Elements.MESSAGE_TYPE_1) => typeOneExtractionStrategy(uuid, msgType)
      case _ => throw new Exception(s"Message type $msgType not supported")
    }
  }

  private def typeZeroExtractionStrategy(uuid: String, msgType: Int): Message = {
    val timeStamp = unpackNextAsLong
    val data = unpackMap

    implicit val formats: DefaultFormats.type = DefaultFormats
    MessageTypeZero(uuid, msgType, timeStamp, Extraction.decompose(data), None)
  }

  private def typeOneExtractionStrategy(uuid: String, msgType: Int): Message = {
    val timeStamp = unpackNextAsLong
    val data = unpackMap
    val hash = unpackHash

    implicit val formats: DefaultFormats.type = DefaultFormats
    MessageTypeOne(uuid, msgType, timeStamp, Extraction.decompose(data), hash)
  }

  private def unpackMap: Map[String, Double] = {
    val size = unpacker.unpackMapHeader()

    val data = unpackKeyValueUntilTheEnd(size)
    data.map { r => (r._1, r._2.toDouble) }.toMap
  }

  private def unpackHash: Option[String] = {
    if (unpacker.hasNext && unpacker.getNextFormat.equals(MessageFormat.BIN8)) {
      val value = unpacker.unpackValue()
      val hashbin = value.asBinaryValue().asByteArray()
      Some(Base64.getEncoder.encodeToString(hashbin))
    } else {
      None
    }
  }

  def unpackKeyValueUntilTheEnd(size: Int, accu: List[(String, String)] = List.empty): List[(String, String)] = {
    if (unpacker.hasNext && size > 0) {
      unpackKeyValueUntilTheEnd(size - 1, accu ++ List(unpackNextValue))
    } else accu
  }

  private def unpackNextValue: (String, String) = {
    val key = unpackNextAsString
    val value: String = unpackToStringDependingOnValue
    (key, value)
  }

  private def unpackToStringDependingOnValue: String = {

    val valueType = unpacker.unpackValue

    valueType.getValueType match {
      case ValueType.STRING => valueType.asStringValue().toString
      case ValueType.INTEGER => valueType.asIntegerValue().toInt.toString
      case ValueType.FLOAT => valueType.asFloatValue().toFloat.toString
      case ValueType.NIL => "0"
      case ValueType.BINARY =>
        val rawByte = valueType.asBinaryValue().asByteArray()
        Hex.encodeHexString(rawByte)
      case _ =>
        logger.error("valueType: " + valueType.getValueType.toString)
        throw new Exception(s"value type ${valueType.getValueType.toString} not supported")
    }
  }

  private def unpackNextAsInt = unpacker.unpackInt()

  private def unpackNextAsLong = unpacker.unpackLong()

  private def unpackNextAsString: String = unpacker.unpackString()

}
