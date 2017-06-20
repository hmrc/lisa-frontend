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
import models.{TaxEnrolmentAddSubscriberBadRequest, TaxEnrolmentAddSubscriberError, TaxEnrolmentAddSubscriberSuccess, TaxEnrolmentAddSubscriberUnauthorised}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, Upstream4xxResponse, Upstream5xxResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TaxEnrolmentServiceSpec extends PlaySpec with MockitoSugar with OneAppPerSuite with TaxEnrolmentJsonFormats {

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val mockConnector:TaxEnrolmentConnector = mock[TaxEnrolmentConnector]

  val SUT = new TaxEnrolmentService {
    override val connector:TaxEnrolmentConnector = mockConnector
  }

  "Tax Enrolment Service" must {
    "return Success" when {
      "the connector returns a HttpResponse with a status of 204" in {
        when(mockConnector.addSubscriber(any(),any())(any())).thenReturn(
            Future.successful(HttpResponse(NO_CONTENT)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe TaxEnrolmentAddSubscriberSuccess
      }
    }
    "return Bad Request" when {
      "the connector returns a Upstream4xxException with a status of 400" in {
        when(mockConnector.addSubscriber(any(),any())(any())).thenReturn(
          Future.failed(Upstream4xxResponse("", 400, 400)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe TaxEnrolmentAddSubscriberBadRequest
      }
    }
    "return Unauthorised" when {
      "the connector returns a Upstream4xxException with a status of 401" in {
        when(mockConnector.addSubscriber(any(),any())(any())).thenReturn(
          Future.failed(Upstream4xxResponse("", 401, 401)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe TaxEnrolmentAddSubscriberUnauthorised
      }
    }
    "return Error" when {
      "the connector returns a HttpResponse with an unexpected status" in {
        when(mockConnector.addSubscriber(any(), any())(any())).thenReturn(
          Future.successful(HttpResponse(OK)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe TaxEnrolmentAddSubscriberError
      }

      "the connector returns a Upstream4xxException with an unexpected status" in {
        when(mockConnector.addSubscriber(any(),any())(any())).thenReturn(
          Future.failed(Upstream4xxResponse("", 404, 404)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe TaxEnrolmentAddSubscriberError
      }

      "the connector returns a unexpected exception" in {
        when(mockConnector.addSubscriber(any(),any())(any())).thenReturn(
          Future.failed(Upstream5xxResponse("", 500, 500)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe TaxEnrolmentAddSubscriberError
      }
    }
  }
}
