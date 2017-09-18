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
import models.{BusinessStructure, TaxEnrolmentDoesNotExist, UserAuthorised, UserDetails}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.{Configuration, Environment, Mode}
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache, ShortLivedCache}

import scala.concurrent.Future


class BusinessStructureControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Business Structure" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val form = new BusinessStructure("LLP")

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(form)))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("checked")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), any())(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must not include ("checked")
      }

    }

  }

  "POST Business Structure" must {

    before {
      reset(mockCache)

      when(mockCache.cache[Any](any(), any(), any())(any(), any())).
        thenReturn(Future.successful(new CacheMap("", Map[String, JsValue]())))
    }

    "return validation errors" when {
      "the submitted data is incomplete" in {
        val uri = controllers.routes.BusinessStructureController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("This field is required")
      }
    }

    "redirect the user to organisation details" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.BusinessStructureController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("businessStructure" -> "LLP")))
        when(mockCache.cache[BusinessStructure](any(),any(),any())(any(),any())).thenReturn(Future.successful(new CacheMap("",Map[String,JsValue]())))
        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
    }

    "store business structure details in cache" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.BusinessStructureController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("businessStructure" -> "LLP")))

        await(SUT.post(request))

        verify(mockCache).cache[BusinessStructure](any(), any(), any())(any(), any())
      }
    }

  }

  val pageTitle = ">Select your business structure</h1>"
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  def createFakePostRequest[T](uri: String, body:T):FakeRequest[T] = {
    addToken(FakeRequest("POST", uri, FakeHeaders(), body))
  }

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  object SUT extends BusinessStructureController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val shortLivedCache: ShortLivedCache = mockCache
    override val sessionCache: SessionCache = mockSessionCache
    override val authorisationService: AuthorisationService = mockAuthorisationService
  }

  when(mockAuthorisationService.userStatus(any())).
    thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}