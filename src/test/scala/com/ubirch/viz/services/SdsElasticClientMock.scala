package com.ubirch.viz.services

import com.sksamuel.elastic4s.Response
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class SdsElasticClientMock extends SdsElasticClient with MockitoSugar {

  override def storeDeviceData(jsonData: String): Future[Response[IndexResponse]] = Future.successful(mock[Response[IndexResponse]])

  override def getLastDeviceData(deviceId: String): Future[Response[SearchResponse]] = Future.successful(mock[Response[SearchResponse]])

  override def getDeviceDataInTimerange(deviceUuid: String, from: String, to: String): Future[Response[SearchResponse]] = Future.successful(mock[Response[SearchResponse]])

  override def getLastNDeviceData(deviceUuid: String, n: Int): Future[Response[SearchResponse]] = Future.successful(mock[Response[SearchResponse]])

}
