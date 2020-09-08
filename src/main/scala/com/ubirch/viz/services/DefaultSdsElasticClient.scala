package com.ubirch.viz.services

import com.sksamuel.elastic4s.{ ElasticClient, ElasticProperties, Index, Response }
import com.sksamuel.elastic4s.http.{ JavaClient, NoOpRequestConfigCallback }
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.config.ConfPaths.EsPaths
import javax.inject.{ Inject, Singleton }
import org.apache.http.auth.{ AuthScope, UsernamePasswordCredentials }
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait SdsElasticClient {

  /**
    * This methods stores the given json data in elasticsearch
    */
  def storeDeviceData(jsonData: String): Future[Response[IndexResponse]]

  /**
    * This methods returns the value with the latest timestamp stored in elasticsearch by the provided device
    */
  def getLastDeviceData(deviceId: String): Future[Response[SearchResponse]]

  def getDeviceDataInTimerange(deviceUuid: String, from: String, to: String): Future[Response[SearchResponse]]

}

@Singleton
class DefaultSdsElasticClient @Inject() (config: Config) extends SdsElasticClient with LazyLogging with EsPaths {

  val callback = new HttpClientConfigCallback {
    override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
      val credentialsProvider = new BasicCredentialsProvider()
      val credentials = new UsernamePasswordCredentials(config.getString(ES_IO_USER), config.getString(ES_PASSWORD))
      credentialsProvider.setCredentials(AuthScope.ANY, credentials)
      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
    }
  }

  val props = ElasticProperties(config.getString(ES_PROTOCOL) + "://" + config.getString(ES_HOST) + ":" + config.getInt(ES_PORT))

  val client = ElasticClient(JavaClient(props, requestConfigCallback = NoOpRequestConfigCallback, httpClientConfigCallback = callback))

  lazy val indexType = Index(config.getString(ES_INDEX)) //IndexAndType(config.elasticIndex, "doc")

  def storeDeviceData(jsonData: String): Future[Response[IndexResponse]] = {
    val res = client.execute {
      indexInto(indexType).doc(jsonData)
    }
    res.onComplete {
      case Success(_) =>
        logger.info(s"successfully writing $jsonData to elasticsearch.")
      case Failure(ex) =>
        logger.error(s"error while writing $jsonData to elasticsearch: ", ex)
    }
    res
  }

  def getLastDeviceData(deviceUuid: String): Future[Response[SearchResponse]] = {
    client.execute {
      //searchWithType(indexType)
      search(indexType)
        .size(1)
        .sortByFieldDesc("timestamp")
        .query(boolQuery()
          .must(s"""uuid("$deviceUuid")"""))
    }
  }

  def getDeviceDataInTimerange(deviceUuid: String, from: String, to: String): Future[Response[SearchResponse]] = {
    client.execute {
      //searchWithType(indexType)
      search(indexType)
        .sortByFieldDesc("timestamp")
        //.query(rangeQuery("timestamp").gte(from).lte(to))
        .query(boolQuery()
          .must(s"""uuid("$deviceUuid")""")
          .filter(rangeQuery("timestamp").gte(from).lte(to)))
        .limit(100)
    }
  }

}
