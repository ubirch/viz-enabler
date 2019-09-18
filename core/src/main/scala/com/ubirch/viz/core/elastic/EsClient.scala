package com.ubirch.viz.core.elastic

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchFlow
import akka.stream.alpakka.elasticsearch.{ElasticsearchSourceSettings, ElasticsearchWriteSettings, MessageWriter, WriteMessage}
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import com.ubirch.viz.core.config.ConfigBase
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object EsClient extends LazyLogging with ConfigBase {

  val credentialsProvider = new BasicCredentialsProvider
  credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password))

  protected lazy implicit val client: RestClient = if (protocol.contains("https")) {
    RestClient.builder(new HttpHost(host, port, protocol))
      .setHttpClientConfigCallback((httpClientBuilder: HttpAsyncClientBuilder) =>
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)).build()
  } else {
    RestClient.builder(new HttpHost(host, port, protocol)).build()
  }

  private implicit val system: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private implicit val sourceSettings: ElasticsearchSourceSettings = ElasticsearchSourceSettings()
    .withBufferSize(10000)
    .withScrollDuration(1.minutes)


  /**
    * This method is executing a scroll search on the elasticsearch.
    *
    * @param jsonData The query of the scroll search.
    * @return an Akka Stream Source with the results
    */
  def storeDeviceData(jsonData: String): Unit = {

    Source(
      collection.immutable.Seq(
        WriteMessage.createIndexMessage(jsonData),
      )
    ).via(
      ElasticsearchFlow.create(
        indexName = index,
        typeName = "_doc",
        ElasticsearchWriteSettings.Default,
        new StupidWriter()
      )
    ).runWith(Sink.seq)
      .onComplete {
        case Success(_) =>
          logger.info(s"successfully writing $jsonData to elasticsearch.")
        case Failure(ex) =>
          logger.error(s"error while writing $jsonData to elasticsearch: ", ex)
      }
  }

  private final class StupidWriter extends MessageWriter[String] {
    override def convert(message: String): String = message
  }


}
