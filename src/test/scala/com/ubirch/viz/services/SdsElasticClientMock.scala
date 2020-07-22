package com.ubirch.viz.services

import com.sksamuel.elastic4s.http.Response
import com.sksamuel.elastic4s.http.index.IndexResponse
import com.sksamuel.elastic4s.http.search.SearchResponse
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class SdsElasticClientMock extends SdsElasticClient with MockitoSugar {

  override def storeDeviceData(jsonData: String): Future[Response[IndexResponse]] = Future.successful(mock[Response[IndexResponse]])

  override def getLastDeviceData(deviceId: String): Future[Response[SearchResponse]] = Future.successful(mock[Response[SearchResponse]])

}
