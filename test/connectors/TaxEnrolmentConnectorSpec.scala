/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import config.AppConfig
import models._

import java.time.{Instant, ZoneId, ZonedDateTime}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TaxEnrolmentConnectorSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite with TaxEnrolmentJsonFormats with SpecBase {

  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val mockHttpClientV2: HttpClientV2 = mock[HttpClientV2]
  val mockAppConfig: AppConfig = mock[AppConfig]
  override implicit val hc: HeaderCarrier = HeaderCarrier()

  when(mockAppConfig.lisaServiceUrl).thenReturn("http://localhost:8886")
  when(mockHttpClientV2.get(any())(any())).thenReturn(mockRequestBuilder)
  when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
  when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)

  val SUT = new TaxEnrolmentConnector(mockHttpClientV2, mockAppConfig)
  val instant: Instant = Instant.ofEpochMilli(1498726914908L)
  val zonedDateTime: ZonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

  private val lisaSuccessSubscription = TaxEnrolmentSubscription(
    created = zonedDateTime,
    lastModified = zonedDateTime,
    credId = "",
    serviceName = "HMRC-LISA-ORG",
    identifiers = List(TaxEnrolmentIdentifier("ZREF", "Z1234")),
    callback = "",
    state = TaxEnrolmentSuccess,
    etmpId = "",
    groupIdentifier = ""
  )

  private val subs = List(lisaSuccessSubscription)

  "Get Subscriptions by Group ID endpoint" must {
    "return whatever it receives" in {
      when(mockRequestBuilder.execute[HttpResponse](any(),any()))
        .thenReturn(Future.successful(HttpResponse(
          status = OK,
          json = Json.toJson(subs),
          headers = Map[String,Seq[String]]("test"->Seq("test1","test2"))
        )))

      val response: Seq[TaxEnrolmentSubscription] = Await.result(SUT.getSubscriptionsByGroupId("1234567890"), Duration.Inf)

      response mustBe subs
    }
  }

}