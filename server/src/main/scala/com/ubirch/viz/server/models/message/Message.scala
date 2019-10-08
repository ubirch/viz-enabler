package com.ubirch.viz.server.models.message

abstract class Message {
  def uuid: String
  def msg_type: Int

  def toJson: String

  def isSameUuid(headerUuid: String): Boolean = headerUuid.equals(uuid)
}
