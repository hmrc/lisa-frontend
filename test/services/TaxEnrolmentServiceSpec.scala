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

  "Tax Enrolment Service" must {

    "return true" when {
      "the connector returns a HttpResponse with a status of 204" in {
        when(mockConnector.addSubscriber(any(),any())(any())).thenReturn(
            Future.successful(HttpResponse(NO_CONTENT)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe true
      }
    }

    "return false" when {
      "the connector returns a HttpResponse with any status other than 204" in {
        when(mockConnector.addSubscriber(any(), any())(any())).thenReturn(
          Future.successful(HttpResponse(OK)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe false
      }

      "the connector returns an exception" in {
        when(mockConnector.addSubscriber(any(),any())(any())).thenReturn(
          Future.failed(Upstream5xxResponse("", 500, 500)))

        val res = Await.result(SUT.addSubscriber("1234567890", "1234567890"), Duration.Inf)

        res mustBe false
      }
    }
  }
}
