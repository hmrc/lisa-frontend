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

package services

import connectors.{TaxEnrolmentConnector, TaxEnrolmentJsonFormats}
import models._

import java.time.ZonedDateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.temporal.ChronoUnit

class TaxEnrolmentServiceSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite
  with TaxEnrolmentJsonFormats
  with MongoSupport {

  override lazy val fakeApplication: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> "false")
    .overrides(
      bind(classOf[MongoComponent]).toInstance(mongoComponent)
    )
    .build()

  implicit val hc:HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val mockConnector:TaxEnrolmentConnector = mock[TaxEnrolmentConnector]

  val SUT = new TaxEnrolmentService(mockConnector)

  "Get LISA subscription" must {

    "return the appropriate state" when {

      "given a single lisa subscription in the connector response" in {
        when(mockConnector.getSubscriptionsByGroupId(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(List(lisaSuccessSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaSuccessSubscription)
      }

      "given two lisa subscriptions in the connector response - newest first" in {
        when(mockConnector.getSubscriptionsByGroupId(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(List(lisaErrorSubscription, lisaSuccessSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaErrorSubscription)
      }

      "given two lisa subscriptions in the connector response - oldest first" in {
        when(mockConnector.getSubscriptionsByGroupId(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(List(lisaSuccessSubscription, lisaErrorSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaErrorSubscription)
      }

      "given multiple different subscriptions in the connector response" in {
        when(mockConnector.getSubscriptionsByGroupId(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(List(lisaSuccessSubscription, lisaErrorSubscription, randomPendingSubscription)))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe Some(lisaErrorSubscription)
      }

    }

    "return a does not exist state" when {

      "there are no lisa subscriptions" in {
        when(mockConnector.getSubscriptionsByGroupId(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(List()))

        val res = Await.result(SUT.getNewestLisaSubscription("1234567890"), Duration.Inf)

        res mustBe None
      }

    }

  }

  private val lisaSuccessSubscription = TaxEnrolmentSubscription(
    created = ZonedDateTime.now(),
    lastModified = ZonedDateTime.now(),
    credId = "",
    serviceName = "HMRC-LISA-ORG",
    identifiers = List(TaxEnrolmentIdentifier("ZREF", "Z1234")),
    callback = "",
    state = TaxEnrolmentSuccess,
    etmpId = "",
    groupIdentifier = ""
  )

  private val lisaErrorSubscription = TaxEnrolmentSubscription(
    created = ZonedDateTime.now().plus(1, ChronoUnit.DAYS),
    lastModified = ZonedDateTime.now(),
    credId = "",
    serviceName = "HMRC-LISA-ORG",
    identifiers = Nil,
    callback = "",
    state = TaxEnrolmentError,
    etmpId = "",
    groupIdentifier = ""
  )

  private val randomPendingSubscription = TaxEnrolmentSubscription(
    created = ZonedDateTime.now().plus(2, ChronoUnit.DAYS),
    lastModified = ZonedDateTime.now(),
    credId = "",
    serviceName = "TEST",
    identifiers = Nil,
    callback = "",
    state = TaxEnrolmentPending,
    etmpId = "",
    groupIdentifier = ""
  )

}
