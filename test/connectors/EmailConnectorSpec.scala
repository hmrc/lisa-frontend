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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsObject
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import scala.concurrent.Future

class EmailConnectorSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach with SpecBase with Injecting {

  val mockHttpClientV2: HttpClientV2 = mock[HttpClientV2]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockMetrics: EmailMetrics = mock[EmailMetrics]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val testEmailConnector = new EmailConnector(mockHttpClientV2, mockAppConfig, mockMetrics)

  override def beforeEach(): Unit = {
    reset(mockHttpClientV2)
    when(mockAppConfig.emailServiceUrl).thenReturn("http://localhost:8886")
    when(mockHttpClientV2.post(any())(any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.withBody(any[JsObject])(any(), any(), any())).thenReturn(mockRequestBuilder)
  }

  "EmailConnector" must {

    "return a 202 accepted" when {

      "correct emailId Id is passed" in {
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val emailString = "test@mail.com"
        val templateId = "lisa_application_submit"
        val params = Map("testParam" -> "testParam")
        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse(ACCEPTED, "")))
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

        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))

        val response = testEmailConnector.sendTemplatedEmail(invalidEmailString, templateId, params)
        await(response) must be(EmailNotSent)

      }
    }

  }
}
