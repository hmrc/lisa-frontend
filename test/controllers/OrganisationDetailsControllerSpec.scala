/*
 * Copyright 2021 HM Revenue & Customs
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

import base.SpecBase
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, MessagesControllerComponents, Request}
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeHeaders, FakeRequest, Injecting}
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.test.CSRFTokenHelper._
import views.html.registration.organisation_details

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OrganisationDetailsControllerSpec extends SpecBase with Injecting {

  val organisationDetailsCacheKey = "organisationDetails"
  val businessStructureCacheKey = "businessStructure"
  val pageTitle = "Your company’s details"

  def createFakePostRequest[T](uri: String, body:T): Request[T] = {
    val request:Request[T] = FakeRequest("POST", uri, FakeHeaders(), body)
    CSRFTokenHelper.addCSRFToken(request)
  }

  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit val organisationDetailsView: organisation_details = inject[organisation_details]
  val SUT = new OrganisationDetailsController()


  "GET Organisation Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")

        when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(organisationDetailsCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(organisationForm)))

        val request = fakeRequest.withCSRFToken
        val result = SUT.get().apply(request)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("Test Company Name")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(organisationDetailsCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val request = fakeRequest.withCSRFToken
        val result = SUT.get().apply(request)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must not include ("value=\'\'")
      }

    }

    "redirect the user to business structure" when {

      "The business structure details are missing from the cache" in {
        when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(organisationDetailsCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }

    }

  }

  "POST Organisation Details" must {

    "return validation errors" when {

      "the submitted data is incomplete" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).
          thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(organisationDetailsCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).
          thenReturn(Future.successful(None))

        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("Enter your registered company name")
        content must include ("Enter your Self Assessment Unique Taxpayer Reference")
      }

      "the company name is invalid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "George?", "ctrNumber" -> "X")))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).
          thenReturn(Future.successful(Some(new BusinessStructure("Corporate Body"))))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(organisationDetailsCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).
          thenReturn(Future.successful(None))

        val result = SUT.post(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("Registered company name must only include letters a to z, numbers 0 to 9, hyphens, spaces and apostrophes")
        content must include ("Corporation Tax Unique Taxpayer Reference must be 10 numbers")
      }

    }

    "redirect the user to trading details" when {

      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "1234567890")))

        when(shortLivedCache.cache[OrganisationDetails](ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(new CacheMap("",Map[String, JsValue]())))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))

        when(rosmService.rosmRegister(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right("3456789")))

        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }

    }

    "return registration error" when {

      "the ROSM registration fails" in {

        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "X")))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).
          thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

        when(rosmService.rosmRegister(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).
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

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))

        when(rosmService.rosmRegister(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right("3456789")))

        await(SUT.post(request))

        verify(shortLivedCache).cache[OrganisationDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(OrganisationDetails.cacheKey), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }

    }

    "store safeId in cache" when {

      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url

        val request = createFakePostRequest[AnyContentAsJson](uri,
          AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "1234567890")))

        when(shortLivedCache.cache[OrganisationDetails](ArgumentMatchers.any(),ArgumentMatchers.eq(organisationDetailsCacheKey),ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(new CacheMap("",Map[String, JsValue]())))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](ArgumentMatchers.any(), ArgumentMatchers.eq(businessStructureCacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(new BusinessStructure("Limited Liability Partnership"))))

        when(rosmService.rosmRegister(ArgumentMatchers.any(),ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Right("3456789")))

        await(SUT.post(request))

        verify(shortLivedCache).cache[OrganisationDetails](ArgumentMatchers.any(), ArgumentMatchers.eq("safeId"), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }

    }

  }
}
