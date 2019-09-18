package com.ubirch.viz.server.authentification

import com.ubirch.viz.server.Models.Elements
import javax.servlet.http.HttpServletRequest
import scalaj.http.Http


object AuthenticateDevice {

  def sendAuth(req: HttpServletRequest): Boolean = {
    val id = req.getHeader(Elements.ID_HEADER)
    val pwd = req.getHeader(Elements.PWD_HEADER)

    val header = Seq((Elements.ID_HEADER, id),
      (Elements.PWD_HEADER, pwd))

    val res = Http("https://api.console.dev.ubirch.com/ubirch-web-ui/api/v1/auth").headers(header).asString
    res.code == 200
  }

  def verifyUpp(data: String): Unit = {

  }

//  def verifyUpp(data: String): Boolean = {
//    val verifier = new DefaultProtocolVerifier(new UbirchKeyService("key service url goes here"))
//    verifier.verify(data.getBytes)
//  }

}
