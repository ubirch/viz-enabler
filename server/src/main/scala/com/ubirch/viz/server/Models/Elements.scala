package com.ubirch.viz.server.Models

case class Device(id: String,
                  password: String)

object Elements{
  val ID_HEADER = "X-Ubirch-Hardware-Id"
  val PWD_HEADER = "X-Ubirch-Credential"
}
