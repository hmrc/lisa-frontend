/*
 * Copyright 2019 HM Revenue & Customs
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
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TaxEnrolmentConnectorSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite with TaxEnrolmentJsonFormats {


  "Get Subscriptions by Group ID endpoint" must {

    "return whatever it receives" in {
      when(mockHttp.GET[HttpResponse](any())(any(), any(), any())).
      thenReturn(Future.successful(HttpResponse(200, Some(Json.toJson(subs)), Map[String,Seq[String]]("test"->Seq("test1","test2")), None)))

      val response = Await.result(SUT.getSubscriptionsByGroupId("1234567890"), Duration.Inf)

      response mustBe subs
    }

  }

  val mockHttp = mock[WSHttp]
  val mockAppConfig = mock[AppConfig]
  implicit val hc = HeaderCarrier()

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

}