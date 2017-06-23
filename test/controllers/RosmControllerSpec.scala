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
import org.mockito.Matchers.{eq => MatcherEquals, _}
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
import services.{AuditService, AuthorisationService, RosmService, TaxEnrolmentService}
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
      reset(mockAuditService)
    }

    val organisationDetailsCacheKey = "organisationDetails"
    val tradingDetailsCacheKey = "tradingDetails"
    val businessStructureCacheKey = "businessStructure"
    val yourDetailsCacheKey = "yourDetails"

    "redirect the user to business structure" when {
      "no business structure details are found in the cache" in {
        val uri = controllers.routes.RosmController.get().url

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }
    }

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in {
        val uri = controllers.routes.RosmController.get().url
        val businessStructureForm = new BusinessStructure("LLP")
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890", Some("5678910"))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

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
        val businessStructureForm = new BusinessStructure("LLP")
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890", Some("5678910"))
        val tradingForm = new TradingDetails( fsrRefNumber = "123", isaProviderRefNumber = "123")

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
        thenReturn(Future.successful(Some(businessStructureForm)))


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
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890", Some("5678910"))
        val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
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

    "handle a successful rosm registration" in {
      val uri = controllers.routes.RosmController.get().url
      val organisationForm = new OrganisationDetails("Test Company Name", "1234567890" , Some("5678910"))
      val tradingForm = new TradingDetails( fsrRefNumber = "123", isaProviderRefNumber = "123")
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

      when (mockRosmService.performSubscription(any())(any())).thenReturn(Future.successful(Right("123456789")))

      when (mockTaxEnrolmentService.addSubscriber(any(), any())(any())).thenReturn(Future.successful(TaxEnrolmentAddSubscriberSucceeded))

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

      redirectLocation(SUT.get(fakeRequest)) must be(Some(routes.ApplicationSubmittedController.get("test@test.com").url))
    }

    "handle a failed rosm registration" when {

      "the ct utr is 0000000000" in {
        val uri = controllers.routes.RosmController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "0000000000", Some("5678910"))
        val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
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

        when (mockRosmService.performSubscription(any())(any())).thenReturn(Future.successful(Left("INTERNAL_SERVER_ERROR")))

        val result = SUT.get(fakeRequest)

        redirectLocation(result) must be(Some(routes.ErrorController.error().url))
      }

    }

    "audit a successful rosm registration" in {
      val uri = controllers.routes.RosmController.get().url
      val organisationForm = new OrganisationDetails("Test Company Name", "1234567890", Some("5678910"))
      val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
      val businessStructureForm = new BusinessStructure("LLP")
      val yourForm = new YourDetails(
        firstName = "Test",
        lastName = "User",
        role = "Role",
        phone = "0191 123 4567",
        email = "test@test.com")
      val registrationDetails = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm)

      when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
        thenReturn(Future.successful(Some(organisationForm)))

      when(mockCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any())).
        thenReturn(Future.successful(Some(tradingForm)))

      when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
        thenReturn(Future.successful(Some(businessStructureForm)))

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
      when (mockRosmService.performSubscription(any())(any())).thenReturn(Future.successful(Right("123456789012")))

      await(SUT.get(fakeRequest))

      verify(mockAuditService).audit(
        auditType = MatcherEquals("applicationReceived"),
        path = MatcherEquals("/lifetime-isa/submit-registration"),
        auditData = MatcherEquals(Map(
          "subscriptionId" -> "123456789012",
          "companyName" -> registrationDetails.organisationDetails.companyName,
          "uniqueTaxReferenceNumber" -> registrationDetails.organisationDetails.ctrNumber,
          "financialServicesRegisterReferenceNumber" -> registrationDetails.tradingDetails.fsrRefNumber,
          "isaProviderReferenceNumber" -> registrationDetails.tradingDetails.isaProviderRefNumber,
          "firstName" -> registrationDetails.yourDetails.firstName,
          "lastName" -> registrationDetails.yourDetails.lastName,
          "roleInOrganisation" -> registrationDetails.yourDetails.role,
          "phoneNumber" -> registrationDetails.yourDetails.phone,
          "emailAddress" -> registrationDetails.yourDetails.email))
      )(any())
    }

    "audit a failed rosm registration" when {

      "the ct utr is 0000000000" in {
        val uri = controllers.routes.RosmController.get().url
        val organisationForm = new OrganisationDetails("Test Company Name", "0000000000", Some("5678910"))
        val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")
        val yourForm = new YourDetails(
          firstName = "Test",
          lastName = "User",
          role = "Role",
          phone = "0191 123 4567",
          email = "test@test.com")
        val registrationDetails = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm)

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), org.mockito.Matchers.eq(organisationDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        when(mockCache.fetchAndGetEntry[TradingDetails](any(), org.mockito.Matchers.eq(tradingDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(tradingForm)))

        when(mockCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(businessStructureCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(businessStructureForm)))

        when(mockCache.fetchAndGetEntry[YourDetails](any(), org.mockito.Matchers.eq(yourDetailsCacheKey))(any(), any())).
          thenReturn(Future.successful(Some(yourForm)))
        when (mockRosmService.performSubscription(any())(any())).thenReturn(Future.successful(Left("INVALID_LISA_MANAGER_REFERENCE_NUMBER")))

        await(SUT.get(fakeRequest))

        verify(mockAuditService).audit(
          auditType = MatcherEquals("applicationNotReceived"),
          path = MatcherEquals("/lifetime-isa/submit-registration"),
          auditData = MatcherEquals(Map(
            "reasonNotReceived" -> "INVALID_LISA_MANAGER_REFERENCE_NUMBER",
            "companyName" -> registrationDetails.organisationDetails.companyName,
            "uniqueTaxReferenceNumber" -> registrationDetails.organisationDetails.ctrNumber,
            "financialServicesRegisterReferenceNumber" -> registrationDetails.tradingDetails.fsrRefNumber,
            "isaProviderReferenceNumber" -> registrationDetails.tradingDetails.isaProviderRefNumber,
            "firstName" -> registrationDetails.yourDetails.firstName,
            "lastName" -> registrationDetails.yourDetails.lastName,
            "roleInOrganisation" -> registrationDetails.yourDetails.role,
            "phoneNumber" -> registrationDetails.yourDetails.phone,
            "emailAddress" -> registrationDetails.yourDetails.email))
        )(any())
      }

    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  val mockRosmConnector: RosmConnector = mock[RosmConnector]
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockAuditService: AuditService = mock[AuditService]
  val mockRosmService: RosmService = mock[RosmService]
  val mockTaxEnrolmentService: TaxEnrolmentService = mock[TaxEnrolmentService]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  object SUT extends RosmController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
    override val auditService: AuditService = mockAuditService
    override val rosmService: RosmService = mockRosmService
    override val taxEnrolmentService: TaxEnrolmentService = mockTaxEnrolmentService
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
