/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.test.CSRFTokenHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SummaryControllerSpec extends SpecBase with Injecting {

  "GET Summary" must {

    val organisationDetailsCacheKey = "organisationDetails"
    val tradingDetailsCacheKey = "tradingDetails"
    val businessStructureCacheKey = "businessStructure"
    val yourDetailsCacheKey = "yourDetails"

    "redirect the user to business structure" when {
      "no business structure details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url

        when(shortLivedCache.fetch(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue]()))))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }
    }

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetch(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm)
          )))))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
      "no safeId is found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetch(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm)
          )))))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
    }

    "redirect the user to trading details" when {
      "no trading details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val businessStructureForm = new BusinessStructure("LLP")



        when(shortLivedCache.fetch(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
            "safeId" -> JsString("")
          )))))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }
    }

    "redirect the user to your details" when {
      "no your details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetch(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
            "safeId" -> JsString(""),
            TradingDetails.cacheKey -> Json.toJson(tradingForm)
          )))))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.YourDetailsController.get().url)
      }
    }

    "show the summary" when {
      "all required details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val tradingForm = new TradingDetails( fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")
        val yourForm = new YourDetails(
          firstName = "Test",
          lastName = "User",
          role = "Role",
          phone = "0191 123 4567",
          email = "test@test.com")

        when(shortLivedCache.fetch(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
            "safeId" -> JsString(""),
            TradingDetails.cacheKey -> Json.toJson(tradingForm),
            YourDetails.cacheKey -> Json.toJson(yourForm)
          )))))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("Check your answers</h1>")
        content must include ("test@test.com")
      }
    }

  }
  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val SUT = new SummaryController()

}
