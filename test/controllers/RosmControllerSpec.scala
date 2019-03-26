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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import base.SpecBase
import models.{BusinessStructure, OrganisationDetails, _}
import org.mockito.Matchers.{eq => MatcherEquals, _}
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RosmControllerSpec extends SpecBase with Injecting {

  "GET Rosm Registration" must {

    val organisationDetailsCacheKey = "organisationDetails"
    val tradingDetailsCacheKey = "tradingDetails"
    val businessStructureCacheKey = "businessStructure"
    val yourDetailsCacheKey = "yourDetails"

    "redirect the user to business structure" when {
      "no data is found in the cache" in {
        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }
      "no business structure details are found in the cache" in {
        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue]()))))

        val result = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get().url)
      }
    }

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in {
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm)
          )))))

        val result = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
      "no safeId details are found in the cache" in {
        val businessStructureForm = new BusinessStructure("LLP")
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")

        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm)
          )))))

        val result = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
    }

    "redirect the user to trading details" when {
      "no trading details are found in the cache" in {
        val businessStructureForm = new BusinessStructure("LLP")
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")

        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
            "safeId" -> JsString("")
          )))))

        val result = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }
    }

    "redirect the user to your details" when {
      "no your details are found in the cache" in {
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
        val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")

        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
            "safeId" -> JsString(""),
            TradingDetails.cacheKey -> Json.toJson(tradingForm)
          )))))

        val result = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.YourDetailsController.get().url)
      }
    }

    "handle a successful rosm registration" in {
      val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
      val tradingForm = new TradingDetails( fsrRefNumber = "123", isaProviderRefNumber = "123")
      val businessStructureForm = new BusinessStructure("LLP")
      val yourForm = new YourDetails(
        firstName = "Test",
        lastName = "User",
        role = "Role",
        phone = "0191 123 4567",
        email = "test@test.com")

      when(shortLivedCache.fetch(any())(any(), any())).
        thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
          BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
          OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
          "safeId" -> JsString(""),
          TradingDetails.cacheKey -> Json.toJson(tradingForm),
          YourDetails.cacheKey -> Json.toJson(yourForm)
        )))))

      when(rosmService.performSubscription(any())(any())).thenReturn(Future.successful(Right("123456789")))

      val rosmAddress = RosmAddress(addressLine1 = "", countryCode = "")
      val rosmContact = RosmContactDetails()
      val rosmSuccessResponse = RosmRegistrationSuccessResponse(
        safeId = "",
        isEditable = true,
        isAnAgent = true,
        isAnIndividual = true,
        address = rosmAddress,
        contactDetails = rosmContact
      )

      redirectLocation(SUT.post(fakeRequest)) must be(Some(routes.ApplicationSubmittedController.get().url))
    }

    "email the user on a successful rosm registration" in {
      reset(emailConnector)

      val testEmail = "success@rosm.subscription"
      val testSubId = "888777666"

      val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
      val tradingForm = new TradingDetails( fsrRefNumber = "123", isaProviderRefNumber = "123")
      val businessStructureForm = new BusinessStructure("LLP")
      val yourForm = new YourDetails(
        firstName = "Test",
        lastName = "User",
        role = "Role",
        phone = "0191 123 4567",
        email = testEmail)

      when(shortLivedCache.fetch(any())(any(), any())).
        thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
          BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
          OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
          "safeId" -> JsString(""),
          TradingDetails.cacheKey -> Json.toJson(tradingForm),
          YourDetails.cacheKey -> Json.toJson(yourForm)
        )))))

      when(rosmService.performSubscription(any())(any())).thenReturn(Future.successful(Right(testSubId)))

      val rosmAddress = RosmAddress(addressLine1 = "", countryCode = "")
      val rosmContact = RosmContactDetails()
      val rosmSuccessResponse = RosmRegistrationSuccessResponse(
        safeId = "",
        isEditable = true,
        isAnAgent = true,
        isAnIndividual = true,
        address = rosmAddress,
        contactDetails = rosmContact
      )

      await(SUT.post(fakeRequest))

      verify(emailConnector).sendTemplatedEmail(
        emailAddress = MatcherEquals(testEmail),
        templateName = MatcherEquals("lisa_application_submit"),
        params = MatcherEquals(Map(
          "application_reference" -> testSubId,
          "email" -> testEmail,
          "review_date" -> LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("d MMMM y")),
          "first_name" -> yourForm.firstName,
          "last_name" -> yourForm.lastName
        ))
      )(any())
    }

    "handle a failed rosm registration" when {
      "the rosm service returns a failure" in {
        val uri = controllers.routes.RosmController.post().url
        val organisationForm = new OrganisationDetails("Test Company Name", "0000000000")
        val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")
        val yourForm = new YourDetails(
          firstName = "Test",
          lastName = "User",
          role = "Role",
          phone = "0191 123 4567",
          email = "test@test.com")

        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
            "safeId" -> JsString(""),
            TradingDetails.cacheKey -> Json.toJson(tradingForm),
            YourDetails.cacheKey -> Json.toJson(yourForm)
          )))))

        when(rosmService.performSubscription(any())(any())).thenReturn(Future.successful(Left("INTERNAL_SERVER_ERROR")))

        val result = SUT.post(fakeRequest)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "audit a successful rosm registration" in {
      val uri = controllers.routes.RosmController.post().url
      val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
      val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
      val businessStructureForm = new BusinessStructure("LLP")
      val yourForm = new YourDetails(
        firstName = "Test",
        lastName = "User",
        role = "Role",
        phone = "0191 123 4567",
        email = "test@test.com")
      val registrationDetails = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm, "123456")

      when(shortLivedCache.fetch(any())(any(), any())).
        thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
          BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
          OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
          "safeId" -> JsString(""),
          TradingDetails.cacheKey -> Json.toJson(tradingForm),
          YourDetails.cacheKey -> Json.toJson(yourForm)
        )))))

      val rosmAddress = RosmAddress(addressLine1 = "", countryCode = "")
      val rosmContact = RosmContactDetails()
      val rosmSuccessResponse = RosmRegistrationSuccessResponse(
        safeId = "",
        isEditable = true,
        isAnAgent = true,
        isAnIndividual = true,
        address = rosmAddress,
        contactDetails = rosmContact
      )

      when(rosmService.performSubscription(any())(any())).thenReturn(Future.successful(Right("123456789012")))

      await(SUT.post(fakeRequest))

      verify(auditService).audit(
        auditType = MatcherEquals("applicationReceived"),
        path = MatcherEquals("/lifetime-isa/submit-registration"),
        auditData = MatcherEquals(Map(
          "subscriptionId" -> "123456789012",
          "companyName" -> registrationDetails.organisationDetails.companyName,
          "utr" -> registrationDetails.organisationDetails.ctrNumber,
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
        val uri = controllers.routes.RosmController.post().url
        val organisationForm = new OrganisationDetails("Test Company Name", "0000000000")
        val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
        val businessStructureForm = new BusinessStructure("LLP")
        val yourForm = new YourDetails(
          firstName = "Test",
          lastName = "User",
          role = "Role",
          phone = "0191 123 4567",
          email = "test@test.com")
        val registrationDetails = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm, "123456")

        when(shortLivedCache.fetch(any())(any(), any())).
          thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
            BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
            OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
            "safeId" -> JsString(""),
            TradingDetails.cacheKey -> Json.toJson(tradingForm),
            YourDetails.cacheKey -> Json.toJson(yourForm)
          )))))

        when(rosmService.performSubscription(any())(any())).thenReturn(Future.successful(Left("INVALID_LISA_MANAGER_REFERENCE_NUMBER")))

        await(SUT.post(fakeRequest))

        verify(auditService).audit(
          auditType = MatcherEquals("applicationNotReceived"),
          path = MatcherEquals("/lifetime-isa/submit-registration"),
          auditData = MatcherEquals(Map(
            "reasonNotReceived" -> "INVALID_LISA_MANAGER_REFERENCE_NUMBER",
            "companyName" -> registrationDetails.organisationDetails.companyName,
            "utr" -> registrationDetails.organisationDetails.ctrNumber,
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

    "cache subscriptionId and email as part of a successful rosm registration" in {
      val uri = controllers.routes.RosmController.post().url
      val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")
      val tradingForm = new TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123")
      val businessStructureForm = new BusinessStructure("LLP")
      val yourForm = new YourDetails(
        firstName = "Test",
        lastName = "User",
        role = "Role",
        phone = "0191 123 4567",
        email = "test@test.com")
      val registrationDetails = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm, "123456")

      when(shortLivedCache.fetch(any())(any(), any())).
        thenReturn(Future.successful(Some(CacheMap("", Map[String, JsValue](
          BusinessStructure.cacheKey -> Json.toJson(businessStructureForm),
          OrganisationDetails.cacheKey -> Json.toJson(organisationForm),
          "safeId" -> JsString(""),
          TradingDetails.cacheKey -> Json.toJson(tradingForm),
          YourDetails.cacheKey -> Json.toJson(yourForm)
        )))))

      val rosmAddress = RosmAddress(addressLine1 = "", countryCode = "")
      val rosmContact = RosmContactDetails()
      val rosmSuccessResponse = RosmRegistrationSuccessResponse(
        safeId = "",
        isEditable = true,
        isAnAgent = true,
        isAnIndividual = true,
        address = rosmAddress,
        contactDetails = rosmContact
      )
      when(rosmService.performSubscription(any())(any())).thenReturn(Future.successful(Right("123456789012")))

      await(SUT.post(fakeRequest))

      val applicationSentVM = ApplicationSent(subscriptionId = "123456789012", email = registrationDetails.yourDetails.email)

      verify(sessionCache).cache(MatcherEquals(ApplicationSent.cacheKey), MatcherEquals(applicationSentVM))(any(), any(), any())
    }

  }
  implicit val mcc = inject[MessagesControllerComponents]
  val SUT = new RosmController()

}