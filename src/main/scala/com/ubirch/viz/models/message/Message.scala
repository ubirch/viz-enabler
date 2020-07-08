package com.ubirch.viz.models.message

abstract class Message {
  def uuid: String
  def msg_type: Int

  def toJson: String

  def hash: Option[String]

  def isSameUuid(headerUuid: String): Boolean = headerUuid.equals(uuid)
}

