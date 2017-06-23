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
import models.{BusinessStructure,OrganisationDetails, TaxEnrolmentDoesNotExist, UserAuthorised, UserDetails}
import org.mockito.Matchers._
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
import services.{RosmService,AuthorisationService}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class OrganisationDetailsControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Organisation Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name",Some("34567889"))

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
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

        content must include (pageTitle)
        content must include ("value=\"\"")
      }

    }

  }

  "POST Organisation Details" must {

    before {
      reset(mockCache)
    }

    "return validation errors" when {
      "the submitted data is incomplete" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("This field is required")
      }

      "the company name is invalid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X @ X", "ctrNumber" -> "X")))
        val result = SUT.post(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("Invalid company name")
      }
    }

    "redirect the user to trading details" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "X")))
        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))
        when (mockRosmService.rosmRegister(any(),any())(any())).thenReturn(Future.successful(Right("3456789")))

        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }
    }

    "return Registration Error" when {
      "the ROSM Registration is failed" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "X")))
        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))
        when(mockRosmService.rosmRegister(any(), any())(any())).thenReturn(Future.successful(Left("SERVICE_UNAVAILABLE")))

        val result = SUT.post(request)

        status(result) mustBe Status.BAD_REQUEST
        val content = contentAsString(result)
        content must include (pageTitle)

        //redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }
    }

    "store organisation details in cache" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "X")))
        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))
        when (mockRosmService.rosmRegister(any(),any())(any())).thenReturn(Future.successful(Right("3456789")))

        await(SUT.post(request))

        verify(mockCache).cache[OrganisationDetails](any(), any(), any())(any(), any())
      }
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val pageTitle = "<h1>Your organisation's name</h1>"
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  def createFakePostRequest[T](uri: String, body:T):FakeRequest[T] = {
    addToken(FakeRequest("POST", uri, FakeHeaders(), body))
  }

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]
  val mockRosmService:RosmService = mock[RosmService]

  object SUT extends OrganisationDetailsController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
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
