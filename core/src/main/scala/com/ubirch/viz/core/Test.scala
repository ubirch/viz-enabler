package com.ubirch.viz.core

object Test {
  def returnStub(upp: UPP): String = {
    s"Content of the UPP is: ${upp.value}"
  }
}
