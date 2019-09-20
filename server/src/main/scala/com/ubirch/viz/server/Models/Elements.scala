package com.ubirch.viz.server.Models

case class Device(
    id: String,
    password: String
)

object Elements {
  val UBIRCH_ID_HEADER = "X-Ubirch-Hardware-Id"
  val UBIRCH_PASSWORD_HEADER = "X-Ubirch-Credential"
  val AUTHORIZATION_SUCCESS_CODE = 200
}
