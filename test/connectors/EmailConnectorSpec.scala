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
import metrics.EmailMetrics
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.Future

class EmailConnectorSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach with SpecBase with Injecting {

  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockMetrics: EmailMetrics = mock[EmailMetrics]

  val testEmailConnector = new EmailConnector(mockHttpClient, mockAppConfig, mockMetrics)

  override def beforeEach(): Unit = {
    reset(mockHttpClient)
  }

  "EmailConnector" must {

    "return a 202 accepted" when {

      "correct emailId Id is passed" in {
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val emailString = "test@mail.com"
        val templateId = "lisa_application_submit"
        val params = Map("testParam" -> "testParam")
        when(mockHttpClient.post(any())(any()).execute[HttpResponse](any(), any())).thenReturn(Future.successful(HttpResponse(ACCEPTED, "")))
        val response = testEmailConnector.sendTemplatedEmail(emailString, templateId, params)
        await(response) must be(EmailSent)

      }
    }

    "return other status" when {

      "incorrect email Id are passed" in {
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val invalidEmailString = "test@test1.com"
        val templateId = "lisa_application_submit"
        val params = Map("testParam" -> "testParam")

        when(mockHttpClient.post(any())(any()).execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))

        val response = testEmailConnector.sendTemplatedEmail(invalidEmailString, templateId, params)
        await(response) must be(EmailNotSent)

      }
    }

  }
}
