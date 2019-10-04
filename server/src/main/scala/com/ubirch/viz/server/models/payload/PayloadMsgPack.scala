package com.ubirch.viz.server.models.payload

import java.util.UUID

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.server.models.{ Elements, Message, MessageTypeZero }
import org.apache.commons.codec.binary.Hex
import org.json4s.{ DefaultFormats, Extraction }
import org.msgpack.core.{ MessagePack, MessageUnpacker }
import org.msgpack.value.ValueType

class PayloadMsgPack(payload: String) extends Payload with LazyLogging {

  val unpacker: MessageUnpacker = getUnpacker

  private def getUnpacker = {
    val messageBytes = hexToByte(payload)
    MessagePack.newDefaultUnpacker(messageBytes)
  }

  private def hexToByte(hex: String): Array[Byte] = {
    Hex.decodeHex(hex)
  }

  def toMessage: Message = {
    removeArrayHeader
    val uuid = getUUID
    val msgType = unpackNextAsInt
    val (timeStamp, data) = extractDependingOnMessageType(msgType)
    implicit val formats: DefaultFormats.type = DefaultFormats
    MessageTypeZero(uuid, msgType, timeStamp, Extraction.decompose(data))
  }

  private def getUUID = {
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

  private def extractDependingOnMessageType(msgType: Int): (Long, Map[String, Double]) = {
    msgType match {
      case code if code.equals(Elements.DEFAULT_MESSAGE_TYPE) => typeZeroExtractionStrategy
      case _ => throw new Exception(s"Message type $msgType not supported")
    }
  }

  private def typeZeroExtractionStrategy: (Long, Map[String, Double]) = {
    val timeStamp = unpackNextAsLong
    val data = unpackMap
    (timeStamp, data)
  }

  private def unpackMap = {
    removeMapHeader

    val data = unpackKeyValueUntilTheEnd()
    data.map { r => (r._1, r._2.toDouble) }.toMap
  }

  def unpackKeyValueUntilTheEnd(accu: List[(String, String)] = List.empty): List[(String, String)] = {
    if (unpacker.hasNext) {
      unpackKeyValueUntilTheEnd(accu ++ List(unpackNextValue))
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

  private def removeMapHeader = unpacker.unpackMapHeader()
  private def removeArrayHeader = unpacker.unpackArrayHeader()
  private def unpackNextAsInt = unpacker.unpackInt()
  private def unpackNextAsLong = unpacker.unpackLong()
  private def unpackNextAsString: String = unpacker.unpackString()

}
