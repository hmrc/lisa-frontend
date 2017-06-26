/*
 * Copyright 2017 HM Revenue & Customs
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

import models._
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TaxEnrolmentConnectorSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite {

  "Get Subscriptions by Group ID endpoint" must {

    "return whatever it receives" in {
      when(mockHttpGet.GET[List[TaxEnrolmentSubscription]](any())(any(), any())).
        thenReturn(Future.successful(subs))

      val response = Await.result(SUT.getSubscriptionsByGroupId("1234567890"), Duration.Inf)

      response mustBe subs
    }

  }

  val mockHttpGet = mock[HttpGet]
  implicit val hc = HeaderCarrier()

  object SUT extends TaxEnrolmentConnector {
    override val httpGet = mockHttpGet
  }

  private val lisaSuccessSubscription = TaxEnrolmentSubscription(
    created = new DateTime(),
    lastModified = new DateTime(),
    credId = "",
    serviceName = "HMRC-LISA-ORG",
    identifiers = Nil,
    callback = "",
    state = TaxEnrolmentSuccess,
    etmpId = "",
    groupIdentifier = ""
  )

  private val subs = List(lisaSuccessSubscription)

}