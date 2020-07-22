package com.ubirch.viz.models

import java.util.Date

import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.search.SearchResponse
import org.joda.time.DateTime
import org.json4s._
import org.json4s.native.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ElasticUtil {

  implicit val formats = Serialization.formats(NoTypeHints)

  def parseData(uuid: String, elasticResponse: Future[Response[SearchResponse]]): Future[ElasticResponse] = {
    for {
      r <- elasticResponse
    } yield {
      if (r.isSuccess) {
        r.result.hits.hits match {
          case Array(hit, _*) =>
            val maybeData = hit.sourceAsMap.get("data")
            val maybeTimestamp = hit.sourceAsMap.get("timestamp") match {
              case Some(timestamp) => Some(DateTime.parse(timestamp.asInstanceOf[String]).toDate)
              case None => None
            }
            maybeData match {
              case Some(data: Map[_, _]) =>
                ElasticResponse(
                  uuid = uuid,
                  ok = true,
                  timestamp = maybeTimestamp,
                  value = data.asInstanceOf[Map[String, String]]
                )
              case Some(badlyFormattedData) =>
                ElasticResponse(uuid = uuid, ok = false, timestamp = maybeTimestamp)
                  .withErrorMessage("Data can not be serialized to a map: " + badlyFormattedData.toString)
              case None =>
                ElasticResponse(uuid = uuid, ok = false, timestamp = maybeTimestamp)
                  .withErrorMessage("Elasticsearch hit found but no data is associated to the latest record")
            }
          case _ => ElasticResponse(uuid = uuid, ok = false)
            .withErrorMessage("No data associated to the latest record")
        }

      } else {
        ElasticResponse(uuid = uuid, ok = false)
          .withErrorMessage("Error getting value from elasticsearch: " + r.error.toString)
      }
    }
  }
}

case class ElasticResponse(uuid: String, ok: Boolean, timestamp: Option[Date] = None, value: Map[String, String] = Map.empty) {
  def withTime(time: Option[Date]): ElasticResponse = copy(timestamp = time)
  def withValue(newValue: Map[String, String]): ElasticResponse = copy(value = newValue)
  def withState(state: Boolean): ElasticResponse = copy(ok = state)
  def withErrorMessage(error: String): ElasticResponse = copy(value = value ++ Map("errorMessage" -> error))
}
