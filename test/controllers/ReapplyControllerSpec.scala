/*
 * Copyright 2018 HM Revenue & Customs
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

import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, status}
import helpers.CSRFTest
import models.{Reapplication, TaxEnrolmentDoesNotExist, UserAuthorised, UserDetails}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsValue
import play.api.{Configuration, Environment, Mode}
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache, ShortLivedCache}
import play.api.test.Helpers._

import scala.concurrent.Future

class ReapplyControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with CSRFTest {

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  "The reapplication controller" should {
    "redirect to the BusinessStructure controller endpoint" in {

      when(mockCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(true)))
      val result = SUT.get(fakeRequest)

      status(result) mustBe Status.SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
    }
  }

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  when(mockAuthorisationService.userStatus(any())).
    thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

  when(mockCache.cache[Any](any(), any(), any())(any(), any(), any())).
    thenReturn(Future.successful(new CacheMap("", Map[String, JsValue]())))

  object SUT extends ReapplyController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val shortLivedCache: ShortLivedCache = mockCache
    override val sessionCache: SessionCache = mockSessionCache
    override val authorisationService: AuthorisationService = mockAuthorisationService
  }

}
