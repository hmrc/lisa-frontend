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

import connectors.UserDetailsConnector
import helpers.CSRFTest
import models._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import services.{AuthorisationService, TaxEnrolmentService}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class SummaryControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Summary" must {

    before {
      reset(mockCache)
    }

    val organisationDetailsCacheKey = "organisationDetails"
    val tradingDetailsCacheKey = "tradingDetails"
    val businessStructureCacheKey = "businessStructure"
    val yourDetailsCacheKey = "yourDetails"

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
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

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(mockCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }
    }

    "redirect the user to business structure" when {
      "no business structure details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
        val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(mockCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(tradingForm)))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }
    }

    "redirect the user to your details" when {
      "no your details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
        val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(mockCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(tradingForm)))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

        when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.YourDetailsController.get().url)
      }
    }

    "show the summary" when {
      "all required details are found in the cache" in {
        val uri = controllers.routes.SummaryController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
        val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")
        val yourForm = new YourDetails(
          firstName = "Test",
          lastName = "User",
          role = "Role",
          phone = "0191 123 4567",
          email = "test@test.com")

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(mockCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(tradingForm)))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

        when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(yourForm)))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Check your answers before submitting your application</h1>")
        content must include ("test@test.com")
      }
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  object SUT extends SummaryController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
    override val authorisationService: AuthorisationService = mockAuthorisationService
  }

  when(mockAuthorisationService.userStatus(any())).
    thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""))))

  when(mockAuthorisationService.getEnrolmentState(any())(any())).
    thenReturn(Future.successful(TaxEnrolmentDoesNotExist))

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}
