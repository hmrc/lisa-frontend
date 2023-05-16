/*
 * Copyright 2023 HM Revenue & Customs
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

import config.AppConfig
import models._
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TaxEnrolmentConnectorSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite with TaxEnrolmentJsonFormats {

  val mockHttp: HttpClient = mock[HttpClient]
  val mockAppConfig: AppConfig = mock[AppConfig]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val SUT = new TaxEnrolmentConnector(mockHttp, mockAppConfig)

  private val lisaSuccessSubscription = TaxEnrolmentSubscription(
    created = new DateTime(),
    lastModified = new DateTime(),
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
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(
          status = OK,
          json = Json.toJson(subs),
          headers = Map[String,Seq[String]]("test"->Seq("test1","test2"))
        )))

      val response = Await.result(SUT.getSubscriptionsByGroupId("1234567890"), Duration.Inf)

      response mustBe subs
    }
  }

}
