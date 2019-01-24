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

package controllers

import java.io.File

import config.AppConfig
import helpers.CSRFTest
import models._
import org.mockito.Matchers.{eq => matcherEq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.{Configuration, Environment, Mode}
import services.{AuthorisationService, RosmService}
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class MatchingFailedControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Matching Failed" must {

    "return a page" in {
      when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(BusinessStructure.cacheKey))(any(), any(), any())).
        thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

      val result = SUT.get(addToken(FakeRequest("GET", "/")))

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include ("Your company’s details could not be found</h1>")
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockMessages: Messages = mock[Messages]

  val SUT = new MatchingFailedController(mockSessionCache, mockCache, mockEnvironment, mockConfig, mockAuthorisationService, mockAppConfig, mockMessages)

  when(mockAuthorisationService.userStatus(any())).
    thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

  when(mockCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))

}
