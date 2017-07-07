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

package services

import connectors.{TaxEnrolmentConnector, TaxEnrolmentJsonFormats}
import models._
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, Upstream5xxResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TaxEnrolmentServiceSpec extends PlaySpec with MockitoSugar with OneAppPerSuite with TaxEnrolmentJsonFormats {

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val mockConnector:TaxEnrolmentConnector = mock[TaxEnrolmentConnector]

  val SUT = new TaxEnrolmentService {
    override val connector:TaxEnrolmentConnector = mockConnector
  }

  "Get LISA subscription" must {

    "return the appropriate state" when {

      "given a single lisa subscription in the connector response" in {
        when(mockConnector.getSubscriptionsByGroupId(any())(any())).thenReturn(
          Future.successful(List(lisaSuccessSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaSuccessSubscription)
      }

      "given two lisa subscriptions in the connector response - newest first" in {
        when(mockConnector.getSubscriptionsByGroupId(any())(any())).thenReturn(
          Future.successful(List(lisaErrorSubscription, lisaSuccessSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaErrorSubscription)
      }

      "given two lisa subscriptions in the connector response - oldest first" in {
        when(mockConnector.getSubscriptionsByGroupId(any())(any())).thenReturn(
          Future.successful(List(lisaSuccessSubscription, lisaErrorSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaErrorSubscription)
      }

      "given multiple different subscriptions in the connector response" in {
        when(mockConnector.getSubscriptionsByGroupId(any())(any())).thenReturn(
          Future.successful(List(lisaSuccessSubscription, lisaErrorSubscription, randomPendingSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaErrorSubscription)
      }

    }

    "return a does not exist state" when {

      "there are no lisa subscriptions" in {
        when(mockConnector.getSubscriptionsByGroupId(any())(any())).thenReturn(
          Future.successful(List()))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe None
      }

    }

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

  private val lisaErrorSubscription = TaxEnrolmentSubscription(
    created = new DateTime().plusDays(1),
    lastModified = new DateTime(),
    credId = "",
    serviceName = "HMRC-LISA-ORG",
    identifiers = Nil,
    callback = "",
    state = TaxEnrolmentError,
    etmpId = "",
    groupIdentifier = ""
  )

  private val randomPendingSubscription = TaxEnrolmentSubscription(
    created = new DateTime().plusDays(2),
    lastModified = new DateTime(),
    credId = "",
    serviceName = "TEST",
    identifiers = Nil,
    callback = "",
    state = TaxEnrolmentPending,
    etmpId = "",
    groupIdentifier = ""
  )

}
