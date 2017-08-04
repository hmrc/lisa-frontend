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

import helpers.CSRFTest
import models._
import org.mockito.Matchers.{eq => matcherEq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.{Configuration, Environment, Mode}
import services.{AuthorisationService, RosmService}
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class MatchingFailedControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Matching Failed" must {

    "return a page" in {
      val result = SUT.get(addToken(FakeRequest("GET", "/")))

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include ("<h1>Your business details could not be found</h1>")
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  object SUT extends MatchingFailedController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val shortLivedCache: ShortLivedCache = mockCache
    override val sessionCache: SessionCache = mockSessionCache
    override val authorisationService: AuthorisationService = mockAuthorisationService
  }

  when(mockAuthorisationService.userStatus(any())).
    thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

}