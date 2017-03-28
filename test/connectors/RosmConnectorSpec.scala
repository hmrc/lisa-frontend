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

import models.RosmRegistration
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RosmConnectorSpec extends PlaySpec
  with MockitoSugar
  with OneAppPerSuite {

  "Register Once endpoint" must {

    "return success" when {
      "rosm returns a success message" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(responseStatus = CREATED, responseJson = None)))

        doRegistrationRequest { response =>
          response mustBe "Failed"
        }
      }
    }

  }

  private def doRegistrationRequest(callback: (String) => Unit) = {
    val request = RosmRegistration(regime = "LISA", requiresNameMatch = false, isAnAgent = false)
    val response = Await.result(SUT.registerOnce(request), Duration.Inf)

    callback(response)
  }

  val mockHttpPost = mock[HttpPost]
  implicit val hc = HeaderCarrier()

  object SUT extends RosmConnector {
    override val httpPost = mockHttpPost
  }
}
