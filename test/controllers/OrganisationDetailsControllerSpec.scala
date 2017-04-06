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
import helpers.CSRFTest
import models.OrganisationDetails
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.Future

class OrganisationDetailsControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest {

  "GET Organisation Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Organisation details</h1>")
        content must include ("Test Company Name")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), any())(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Organisation details</h1>")
        content must include ("value=\"\"")
      }

    }

  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  val mockAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]
  val mockRosmConnector: RosmConnector = mock[RosmConnector]
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]

  object SUT extends OrganisationDetailsController {
    override val authConnector: PlayAuthConnector = mockAuthConnector
    override val rosmConnector: RosmConnector = mockRosmConnector
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
  }

  when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
    thenReturn(Future.successful(Some("1234")))

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}