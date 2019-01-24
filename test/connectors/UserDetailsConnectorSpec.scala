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

import models._
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class UserDetailsConnectorSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite {

  implicit val hc = HeaderCarrier()

  "Get User Details endpoint" must {

    "return whatever it receives" in {
      when(mockHttp.GET[UserDetails](any())(any(), any(), any())).
        thenReturn(Future.successful(UserDetails(None, None, "")))

      val response = Await.result(SUT.getUserDetails("1234567890"), Duration.Inf)

      response mustBe UserDetails(None, None, "")
    }

  }

  val mockHttp = mock[WSHttp]
  val mockConfiguration = mock[Configuration]
  val mockEnvironment = mock[Environment]

  val SUT = new UserDetailsConnector(mockHttp, mockConfiguration, mockEnvironment)

}