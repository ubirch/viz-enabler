package com.ubirch.viz.server.models.payload

import com.ubirch.viz.server.models.{Elements, Message, MessageTypeZero}
import org.apache.commons.codec.binary.Hex
import org.msgpack.core.{MessagePack, MessageUnpacker}
import org.msgpack.value.ValueType



class PayloadMsgPack(payload: String) extends Payload {

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
    val uuid = unpackNextAsString
    val msgType = unpackNextAsInt
    val (timeStamp, data) = extractDependingOnMessageType(msgType)
    MessageTypeZero(uuid, msgType, timeStamp, data)
  }

  private def extractDependingOnMessageType(msgType: Int): (Long, Map[String, Double]) = {
    msgType match {
      case code if code.equals(Elements.DEFAULT_MESSAGE_TYPE) => defaultExtractionStrategy
      case _ => throw new Exception(s"Message type $msgType not supported")
    }
  }

  private def defaultExtractionStrategy: (Long, Map[String, Double]) = {
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
    val value: String = unpackDependingOnValue
    (key, value)
  }

  private def unpackDependingOnValue: String = {
    val valueType = unpacker.unpackValue
    valueType.getValueType match {
      case ValueType.STRING => valueType.asStringValue().toString
      case ValueType.INTEGER => valueType.asIntegerValue().toInt.toString
      case ValueType.FLOAT => valueType.asFloatValue().toFloat.toString
      case _ =>
        println(valueType.getValueType.toString)
        throw new Exception("value type not recognized")
    }
  }

  private def stopIfNextStringIsNot(stringShouldBe: String): Unit = {
    val nextString = unpackNextAsString
    stopIfStringNotCorrect(nextString, stringShouldBe)
  }

  private def stopIfStringNotCorrect(string: String, shouldBe: String): Unit = {
    if (!string.equals(shouldBe)) throw new Exception(s"$string not equal $shouldBe")
  }

  private def removeMapHeader = unpacker.unpackMapHeader()
  private def removeArrayHeader = unpacker.unpackArrayHeader()
  private def unpackNextAsInt = unpacker.unpackInt()
  private def unpackNextAsLong = unpacker.unpackLong()
  private def unpackNextAsString: String = unpacker.unpackString()

}
