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

import base.SpecBase
import helpers.CSRFTest
import models._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import play.api.http.Status
import play.api.test.Helpers._

import scala.concurrent.Future

class SummaryControllerSpec extends SpecBase
  with CSRFTest
  with BeforeAndAfter {

  "GET Summary" must {

    before {
      reset(shortLivedCache)
      when(shortLivedCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))
    }

    val organisationDetailsCacheKey = "organisationDetails"
    val tradingDetailsCacheKey = "tradingDetails"
    val businessStructureCacheKey = "businessStructure"
    val yourDetailsCacheKey = "yourDetails"

    "redirect the user to business structure" when {
      "no business structure details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }
    }

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
      "no safeId is found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(shortLivedCache.fetchAndGetEntry[String](any(), org.mockito.Matchers.eq("safeId"))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
    }

    "redirect the user to trading details" when {
      "no trading details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
        thenReturn(Future.successful(Some(businessStructureForm)))

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
        thenReturn(Future.successful(Some(organisationForm)))

        when(shortLivedCache.fetchAndGetEntry[String](any(), org.mockito.Matchers.eq("safeId"))(any(), any(), any())).
          thenReturn(Future.successful(Some("123456")))

        when(shortLivedCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any(), any())).
        thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

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

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(shortLivedCache.fetchAndGetEntry[String](any(), org.mockito.Matchers.eq("safeId"))(any(), any(), any())).
          thenReturn(Future.successful(Some("123456")))

        when(shortLivedCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(tradingForm)))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

        when(shortLivedCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

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

        when(shortLivedCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(shortLivedCache.fetchAndGetEntry[String](any(), org.mockito.Matchers.eq("safeId"))(any(), any(), any())).
          thenReturn(Future.successful(Some("123456")))

        when(shortLivedCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(tradingForm)))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

        when(shortLivedCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(yourForm)))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("Check your answers</h1>")
        content must include ("test@test.com")
      }
    }

  }

  val SUT = new SummaryController()

}
