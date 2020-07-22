package com.ubirch.viz.rest

import com.sksamuel.elastic4s.http.index.IndexResponse
import com.sksamuel.elastic4s.http.Response
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.authentification.AuthClient
import com.ubirch.viz.models.{ ElasticUtil, Elements }
import com.ubirch.viz.models.message.{ Message, MessageTypeZero }
import com.ubirch.viz.models.payload.{ PayloadFactory, PayloadType }
import com.ubirch.viz.models.payload.PayloadType.PayloadType
import com.ubirch.viz.services.SdsElasticClient
import javax.inject.{ Inject, Singleton }
import org.json4s.{ DefaultFormats, Formats }
import org.json4s.JsonDSL._
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{ Swagger, SwaggerSupport, SwaggerSupportSyntax }
import org.scalatra.{ CorsSupport, FutureSupport, ScalatraServlet }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

@Singleton
class ApiRest @Inject() (elasticClient: SdsElasticClient, authClient: AuthClient)(implicit val swagger: Swagger) extends ScalatraServlet
  with NativeJsonSupport with SwaggerSupport with CorsSupport with LazyLogging with FutureSupport {

  // Allows CORS support to display the swagger UI when using the same network
  options("/*") {
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT")
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
  }

  // Stops the APIJanusController from being abstract
  protected val applicationDescription = "Simple Data Service"

  // Sets up automatic case class to JSON output serialization
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  protected implicit def executor: ExecutionContextExecutor = ExecutionContext.global

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
    stopIfNotAuthorized()
  }

  val hwDeviceIdHeaderSwagger: SwaggerSupportSyntax.ParameterBuilder[String] = headerParam[String](Elements.UBIRCH_ID_HEADER).
    description("HardwareId of the device")
  val passwordHeaderSwagger: SwaggerSupportSyntax.ParameterBuilder[String] = headerParam[String](Elements.UBIRCH_PASSWORD_HEADER).
    description("Password of the device, base64 encoded")

  val postDataJson: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendJson")
      summary "Send a JSON"
      schemes ("http", "https") // Force swagger ui to use http OR https, only need to say it once
      description "Send a JSON message to the Ubirch Simple Data Service"
      tags "send"
      parameters (
        bodyParam[MessageTypeZero]("payload").
        description(
          """Payload to be stored, json format. Should have this format:
            | {"uuid": uuid, "msg_type": 0,"timestamp": EPOCH_SECONDS, "data": Map[String, Double]}""".stripMargin
        ),
          hwDeviceIdHeaderSwagger,
          passwordHeaderSwagger
      ))

  post("/json", operation(postDataJson)) {
    writeInEs(PayloadType.Json)
    ""
  }

  val postDataMsgPack: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("sendMsgPack")
      summary "Send a msgPack"
      description "Send a msgPack to the Ubirch Simple Data Service"
      tags "send"
      parameters (
        bodyParam[MessageTypeZero]("payload").
        description(
          """Payload to be stored, msgpack format. Should have this format:
            | {"uuid": uuid, "msg_type": 0,"timestamp": EPOCH_SECONDS, "data": Map[String, Double]}""".stripMargin
        ),
          hwDeviceIdHeaderSwagger,
          passwordHeaderSwagger
      ))

  post("/msgpack", operation(postDataMsgPack)) {
    writeInEs(PayloadType.MsgPack)
    ""
  }

  post("/msgPack", operation(postDataMsgPack)) {
    writeInEs(PayloadType.MsgPack)
    ""
  }

  val getLastMessageFromUUID: SwaggerSupportSyntax.OperationBuilder =
    (apiOperation[String]("getMessage")
      summary "Get a message"
      description "Get the last payload sent by the device with the specified uuid to the SDS."
      tags "send"
      parameters (
        pathParam("uuid").description("uuid of the device"),
        hwDeviceIdHeaderSwagger,
        passwordHeaderSwagger
      ))

  get("/:uuid", operation(getLastMessageFromUUID)) {
    val uuid = params("uuid")
    logIncomingRoad(s"get(/$uuid)")
    val elasticResponse = elasticClient.getLastDeviceData(uuid)
    ElasticUtil.parseData(uuid, elasticResponse)
  }

  private def writeInEs(payloadType: PayloadType): Future[Response[IndexResponse]] = {
    val payload = getPayload
    logIncomingRoad(s"post(/${payloadType.toString})", s"payload = $payload")
    val message = payloadToMessage(payload, payloadType)
    stopIfUuidsAreDifferent(message)
    sendMessageToElasticSearch(message)
  }

  private def sendMessageToElasticSearch(message: Message): Future[Response[IndexResponse]] = {
    val jsonForEs = messageToJson(message)
    elasticClient.storeDeviceData(jsonForEs)
  }

  private def messageToJson(message: Message) = {
    try {
      message.toJson
    } catch {
      case e: java.lang.StringIndexOutOfBoundsException =>
        logger.error(createServerError("Parsing payload", e.getClass.toString + ": " + e.getMessage))
        halt(Elements.DEFAULT_ERROR_CODE, createServerError("Parsing payload", e.getClass.toString + ": " + e.getMessage))
    }
  }

  private def payloadToMessage(payload: String, payloadType: PayloadType) = {
    try {
      val payloadStruct = PayloadFactory(payload, payloadType)
      payloadStruct.toMessage
    } catch {
      case e: Throwable =>
        logger.error(createServerError("Parsing payload", e.getClass.toString + ":" + e.getMessage))
        halt(Elements.DEFAULT_ERROR_CODE, createServerError("Parsing payload", e.getClass.toString + ":" + e.getMessage))
    }
  }

  private def stopIfNotAuthorized(): Unit = {
    logger.debug("checking device auth")

    val keyCloakAuthenticationResponse = authClient.createRequestAndGetAuthorizationResponse(request)
    if (!authClient.isAuthorisationCodeCorrect(keyCloakAuthenticationResponse)) {
      logger.warn(s"Device not authorized")
      halt(Elements.NOT_AUTHORIZED_CODE, createServerError(Elements.AUTHENTICATION_ERROR_NAME, keyCloakAuthenticationResponse.body))
    }
  }

  private def stopIfUuidsAreDifferent(message: Message): Unit = {
    if (!message.isSameUuid(request.getHeader(Elements.UBIRCH_ID_HEADER))) {
      logger.warn(s"""{"WARN": "UUIDs in header and payload different"}""")
      halt(Elements.NOT_AUTHORIZED_CODE, createServerError(Elements.AUTHENTICATION_ERROR_NAME, Elements.MESSAGE_ERROR_DIFFERENT_UUID))
    }
  }

  private def getPayload: String = {
    val message = request.body
    stopIfMessageEmpty(message)
    message
  }

  private def stopIfMessageEmpty(message: String): Unit = {
    if (message.isEmpty) {
      throw new Exception("Message is empty")
    }
  }

  private def logIncomingRoad(roadName: String, additionalInfo: String = ""): Unit = {
    val loggerMessage = ("road" -> roadName) ~ ("additionalInformation" -> additionalInfo)
    logger.debug(compact(render(loggerMessage)))
  }

  error {
    case e =>
      logger.error(createServerError(e.getClass.toString, e.getMessage))
      halt(Elements.DEFAULT_ERROR_CODE, createServerError(e.getClass.toString, e.getMessage))
  }

  def createServerError(errorType: String, message: String): String = {
    val errorMessage = "error" ->
      ("error type" -> errorType) ~
      ("message" -> message.replaceAll(System.lineSeparator, ""))
    compact(render(errorMessage))
  }

}
