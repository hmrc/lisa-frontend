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

package controllers

import java.io.File

import connectors.RosmConnector
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.Future


class RegistrationControllerSpec extends PlaySpec
  with OneAppPerSuite
  with MockitoSugar {

  val fakeRequest = FakeRequest("GET", "/")

  val mockAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]
  val mockRosmConnector: RosmConnector = mock[RosmConnector]
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]

  "GET Organisation Details" should {

    when(mockCache.fetchAndGetEntry(any(), any())(any(), any())).thenReturn(Future.successful(None))

    when(mockConfig.getString(matches("^appName$"), any())).
      thenReturn(Some("lisa-frontend"))

    when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
      thenReturn(Some(""))

    when(mockConfig.getString(matches("^sosOrigin$"), any())).
      thenReturn(None)

    "redirect to login" when {
      "a missing bearer token is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new MissingBearerToken()))

        val result = SUT.organisationDetails(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        val redirectUrl = redirectLocation(result).getOrElse("")

        redirectUrl must startWith("/gg/sign-in?continue=%2Flifetime-isa")
        redirectUrl must endWith("&origin=lisa-frontend")
      }
    }

  }

  val SUT = new Registration {
    override val authConnector: PlayAuthConnector = mockAuthConnector
    override val rosmConnector: RosmConnector = mockRosmConnector
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
  }

}
