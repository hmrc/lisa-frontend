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

import helpers.CSRFTest
import models._
import org.mockito.Matchers.{eq => matcherEq, _}
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
import services.{AuthorisationService, RosmService}
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache, ShortLivedCache}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class OrganisationDetailsControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  val organisationDetailsCacheKey = "organisationDetails"
  val businessStructureCacheKey = "businessStructure"

  "GET Organisation Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")

        when(mockCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some((organisationForm))))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("Test Company Name")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(mockCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("value=\"\"")
      }

    }

    "redirect the user to business structure" when {

      "The business structure details are missing from the cache" in {
        when(mockCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }

    }

  }

  "POST Organisation Details" must {

    before {
      reset(mockCache)

      when(mockCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))

      when(mockCache.cache[Any](any(), any(), any())(any(), any(), any())).
        thenReturn(Future.successful(new CacheMap("", Map[String, JsValue]())))
    }

    "return validation errors" when {

      "the submitted data is incomplete" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("This field is required")
      }

      "the company name is invalid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "George?", "ctrNumber" -> "X")))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Corporate Body"))))

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.post(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
      }

    }

    "redirect the user to trading details" when {

      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "1234567890")))

        when(mockCache.cache[OrganisationDetails](any(),any(),any())(any(), any(), any())).thenReturn(Future.successful(new CacheMap("",Map[String, JsValue]())))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))

        when(mockRosmService.rosmRegister(any(),any())(any())).
          thenReturn(Future.successful(Right("3456789")))

        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }

    }

    "return registration error" when {

      "the ROSM registration fails" in {

        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "X")))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(mockRosmService.rosmRegister(any(), any())(any())).
          thenReturn(Future.successful(Left("SERVICE_UNAVAILABLE")))

        val result = SUT.post(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
      }

    }

    "store organisation details in cache" when {

      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url

        val request = createFakePostRequest[AnyContentAsJson](uri,
          AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "1234567890")))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))

        when(mockRosmService.rosmRegister(any(),any())(any())).
          thenReturn(Future.successful(Right("3456789")))

        await(SUT.post(request))

        verify(mockCache).cache[OrganisationDetails](any(), matcherEq(OrganisationDetails.cacheKey), any())(any(), any(), any())
      }

    }

    "store safeId in cache" when {

      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url

        val request = createFakePostRequest[AnyContentAsJson](uri,
          AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "1234567890")))

        when(mockCache.cache[OrganisationDetails](any(),org.mockito.Matchers.eq(organisationDetailsCacheKey),any())(any(), any(), any())).thenReturn(Future.successful(new CacheMap("",Map[String, JsValue]())))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))

        when(mockRosmService.rosmRegister(any(),any())(any())).
          thenReturn(Future.successful(Right("3456789")))

        await(SUT.post(request))

        verify(mockCache).cache[OrganisationDetails](any(), matcherEq("safeId"), any())(any(), any(), any())
      }

    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val pageTitle = "Your company’s details</h1>"
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  def createFakePostRequest[T](uri: String, body:T):FakeRequest[T] = {
    addToken(FakeRequest("POST", uri, FakeHeaders(), body))
  }

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]
  val mockRosmService:RosmService = mock[RosmService]

  object SUT extends OrganisationDetailsController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val shortLivedCache: ShortLivedCache = mockCache
    override val sessionCache: SessionCache = mockSessionCache
    override val authorisationService: AuthorisationService = mockAuthorisationService
    override val rosmService:RosmService = mockRosmService

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
