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

import connectors.RosmConnector
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
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class RosmControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Rosm Registration" must {

    before {
      reset(mockCache)
      reset(mockRosmConnector)
    }

    val organisationDetailsCacheKey = "organisationDetails"
    val tradingDetailsCacheKey = "tradingDetails"
    val yourDetailsCacheKey = "yourDetails"

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in {
        val uri = controllers.routes.RosmController.get().url

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
    }

    "redirect the user to trading details" when {
      "no trading details are found in the cache" in {
        val uri = controllers.routes.RosmController.get().url
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

    "redirect the user to your details" when {
      "no your details are found in the cache" in {
        val uri = controllers.routes.RosmController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
        val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(mockCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(tradingForm)))

        when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.YourDetailsController.get().url)
      }
    }

    "submit the registration" when {
      "all required details are found in the cache" in {
        val uri = controllers.routes.RosmController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
        val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")
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

        when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(yourForm)))

        await(SUT.get(fakeRequest))

        verify(mockRosmConnector).registerOnce(any(), any())(any())
      }
    }

    "handle a successful rosm registration" in {
      val uri = controllers.routes.RosmController.get().url
      val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
      val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")
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

      when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
        thenReturn(Future.successful(Some(yourForm)))

      val rosmAddress = RosmAddress(addressLine1 = "", countryCode = "")
      val rosmContact = RosmContactDetails()
      val rosmSuccessResponse = RosmRegistrationSuccessResponse(
        safeId = "",
        agentReferenceNumber = "",
        isEditable = true,
        isAnAgent = true,
        isAnASAgent = true,
        isAnIndividual = true,
        address = rosmAddress,
        contactDetails = rosmContact
      )

      when(mockRosmConnector.registerOnce(any(), any())(any())).thenReturn(Future.successful(rosmSuccessResponse))

      status(SUT.get(fakeRequest)) mustBe NOT_IMPLEMENTED
    }

    "handle a failed rosm registration" when {
      "the proper failure response is returned" in {
        val uri = controllers.routes.RosmController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
        val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")
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

        when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(yourForm)))

        val rosmFailedResponse = RosmRegistrationFailureResponse(code = "failed", reason = "failed to register")

        when(mockRosmConnector.registerOnce(any(), any())(any())).thenReturn(Future.successful(rosmFailedResponse))

        val result = SUT.get(fakeRequest)

        status(result) mustBe INTERNAL_SERVER_ERROR

        (contentAsJson(result) \ "reason").as[String] mustBe "failed to register"
      }
      "the future fails" in {
        val uri = controllers.routes.RosmController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "Test Trading Name")
        val tradingForm = new TradingDetails(ctrNumber = "1234567890", fsrRefNumber = "123", isaProviderRefNumber = "123")
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

        when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(yourForm)))

        when(mockRosmConnector.registerOnce(any(), any())(any())).thenReturn(Future.failed(new RuntimeException("test")))

        val result = SUT.get(fakeRequest)

        status(result) mustBe INTERNAL_SERVER_ERROR

        contentAsString(result) must include ("<h1>An error occurred</h1>")
      }
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  val mockAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]
  val mockRosmConnector: RosmConnector = mock[RosmConnector]
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]

  object SUT extends RosmController {
    override val authConnector: PlayAuthConnector = mockAuthConnector
    override val rosmConnector: RosmConnector = mockRosmConnector
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
  }

  when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
    thenReturn(Future.successful(Some("1234")))

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}
