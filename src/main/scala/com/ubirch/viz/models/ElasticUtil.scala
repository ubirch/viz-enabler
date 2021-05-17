package com.ubirch.viz.models

import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.searches.{ SearchHit, SearchResponse }
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime
import org.json4s._
import org.json4s.native.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ElasticUtil extends LazyLogging {

  implicit val formats: AnyRef with Formats = Serialization.formats(NoTypeHints)

  def parseSingleData(uuid: String, elasticResponse: Future[Response[SearchResponse]]): Future[ElasticResponse] = {
    for {
      r <- elasticResponse
    } yield {
      if (r.isSuccess) {
        r.result.hits.hits match {
          case Array(hit, _*) =>
            val maybeData = hit.sourceAsMap.get("data")
            val maybeMsgType = hit.sourceAsMap.get("msg_type") match {
              case Some(value) => value.asInstanceOf[Int]
              case None => -1
            }
            val maybeTimestamp = hit.sourceAsMap.get("timestamp") match {
              case Some(timestamp) => Some(DateTime.parse(timestamp.asInstanceOf[String]).getMillis / 1000)
              case None => None
            }
            maybeData match {
              case Some(data: Map[_, _]) =>
                ElasticResponse(
                  uuid = uuid,
                  timestamp = maybeTimestamp,
                  data = data.asInstanceOf[Map[String, String]],
                  msg_type = maybeMsgType
                )
              case Some(badlyFormattedData) =>
                ElasticResponse(uuid = uuid, timestamp = maybeTimestamp)
                  .withErrorMessage("Data can not be serialized to a map: " + badlyFormattedData.toString)
              case None =>
                ElasticResponse(uuid = uuid, timestamp = maybeTimestamp)
                  .withErrorMessage("Elasticsearch hit found but no data is associated to the latest record")
            }
          case _ => ElasticResponse(uuid = uuid)
            .withErrorMessage("No data associated to the latest record")
        }

      } else {
        ElasticResponse(uuid = uuid)
          .withErrorMessage("Error getting value from elasticsearch: " + r.error.toString)
      }
    }
  }

  def parseMultipleData(uuid: String, elasticResponse: Future[Response[SearchResponse]]): Future[ElasticResponses] = {
    for {
      r <- elasticResponse
    } yield {
      if (r.isSuccess) {
        r.result.hits.hits match {
          case Array(_, _*) =>
            forAll(r.result.hits.hits.toList, ElasticResponses(), uuid)
          case _ => ElasticResponses().withResponse(ElasticResponse(uuid = uuid)
            .withErrorMessage("No data associated to the latest record"))
        }

      } else {
        ElasticResponses().withResponse(ElasticResponse(uuid = uuid)
          .withErrorMessage("Error getting value from elasticsearch: " + r.error.toString))
      }
    }
  }

  private def forAll(hits: List[SearchHit], analyzedResponses: ElasticResponses, uuid: String): ElasticResponses = {
    hits match {
      case ::(hit, tl) =>
        val maybeData = hit.sourceAsMap.get("data")
        val maybeMsgType = hit.sourceAsMap.get("msg_type") match {
          case Some(value) => value.asInstanceOf[Int]
          case None => -1
        }
        val maybeTimestamp = hit.sourceAsMap.get("timestamp") match {
          case Some(timestamp) => Some(DateTime.parse(timestamp.asInstanceOf[String]).getMillis / 1000)
          case None => None
        }
        val newResponse = maybeData match {
          case Some(foundData: Map[_, _]) =>
            ElasticResponse(
              uuid = uuid,
              timestamp = maybeTimestamp,
              data = foundData.asInstanceOf[Map[String, String]],
              msg_type = maybeMsgType
            )
          case Some(badlyFormattedData) =>
            ElasticResponse(uuid = uuid, timestamp = maybeTimestamp)
              .withErrorMessage("Data can not be serialized to a map: " + badlyFormattedData.toString)
          case None =>
            ElasticResponse(uuid = uuid, timestamp = maybeTimestamp)
              .withErrorMessage("Elasticsearch hit found but no data is associated to the latest record")
        }
        forAll(tl, analyzedResponses.withResponse(newResponse), uuid)
      case Nil => analyzedResponses
    }
  }
}

case class ElasticResponse(uuid: String, timestamp: Option[Long] = None, data: Map[String, String] = Map.empty, msg_type: Int = -1) {
  def withTime(time: Option[Long]): ElasticResponse = copy(timestamp = time)
  def withValue(newValue: Map[String, String]): ElasticResponse = copy(data = newValue)
  def withErrorMessage(error: String): ElasticResponse = copy(data = data ++ Map("errorMessage" -> error))
  def withMessageType(msgType: Int): ElasticResponse = copy(msg_type = msgType)
}

case class ElasticResponses(responses: List[ElasticResponse] = Nil) {
  def withResponse(newResponse: ElasticResponse): ElasticResponses = copy(responses = responses ++ List(newResponse))
}
