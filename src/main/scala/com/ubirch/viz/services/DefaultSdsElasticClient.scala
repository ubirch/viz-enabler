package com.ubirch.viz.services

import com.sksamuel.elastic4s.http.{ ElasticClient, ElasticProperties, Response }
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.IndexAndType
import com.sksamuel.elastic4s.http.index.IndexResponse
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.config.ConfigProvider
import javax.inject.{ Inject, Singleton }
import org.apache.http.auth.{ AuthScope, UsernamePasswordCredentials }
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.{ HttpClientConfigCallback, RequestConfigCallback }

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

}

@Singleton
class DefaultSdsElasticClient @Inject() (config: ConfigProvider) extends SdsElasticClient with LazyLogging {

  lazy val provider: BasicCredentialsProvider = {
    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(config.username, config.password)
    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }

  val client = ElasticClient(ElasticProperties(config.host + ":" + config.elasticPort), new RequestConfigCallback {
    override def customizeRequestConfig(requestConfigBuilder: Builder): Builder = {
      requestConfigBuilder
    }
  }, new HttpClientConfigCallback {
    override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
      httpClientBuilder.setDefaultCredentialsProvider(provider)
    }
  })

  lazy val indexType = IndexAndType(config.elasticIndex, "doc")

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
      searchWithType(indexType)
        .size(1)
        .sortByFieldDesc("timestamp")
        .query(boolQuery()
          .must(s"""uuid("$deviceUuid")"""))
    }
  }

}
