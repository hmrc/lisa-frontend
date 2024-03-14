/*
 * Copyright 2023 HM Revenue & Customs
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
import helpers.FullCacheTest
import helpers.FullCacheTestData._
import models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.mongo.cache.DataKey
import views.html.error_template

import scala.concurrent.Future

class RosmControllerSpec extends SpecBase with Injecting {

  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit val errorView: error_template = inject[error_template]
  val SUT = new RosmController()

  val customTestEmail = "success@rosm.subscription"
  val customYourForm = new YourDetails(
    firstName = "Test",
    lastName = "User",
    role = "Role",
    phone = "0191 123 4567",
    email = customTestEmail
  )
  val yourFormKeyAndJsonSuccessEmail: (String, JsValue) = YourDetails.cacheKey -> Json.toJson(customYourForm)

  val customDataComponents: Seq[(String, JsValue)] = Seq(
    organisationFormKeyAndJson,
    tradingFormKeyAndJson,
    businessStructureFormKeyAndJson,
    safeKeyAndJson,
    yourFormKeyAndJsonSuccessEmail
  )

  "GET Rosm Registration" must {

    "redirect the user to business structure" when {
      "no data is found in the cache" in new FullCacheTest(Seq.empty[(String, JsValue)]) {

        val result: Future[Result] = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get.url)
      }
      "no business structure details are found in the cache" in new FullCacheTest(noBusinessStructureComponents) {

        val result: Future[Result] = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get.url)
      }
    }

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in new FullCacheTest(noOrgDetailsComponents) {

        val result: Future[Result] = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get.url)
      }

      "no safeId details are found in the cache" in new FullCacheTest(noSafeIdComponents) {

        val result: Future[Result] = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get.url)
      }
    }

    "redirect the user to trading details" when {
      "no trading details are found in the cache" in new FullCacheTest(noTradingDetailsComponents){

        val result: Future[Result] = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get.url)
      }
    }

    "redirect the user to your details" when {
      "no your details are found in the cache" in new FullCacheTest(noFormDetailComponents) {

        val result: Future[Result] = SUT.post(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.YourDetailsController.get.url)
      }
    }

    "handle a successful rosm registration" in new FullCacheTest(allDataComponents) {

      when(rosmService.performSubscription(any)(any))
        .thenReturn(Future.successful(Right("123456789")))

      redirectLocation(SUT.post(fakeRequest)) must be(Some(routes.ApplicationSubmittedController.get.url))
    }

    "email the user on a successful rosm registration" in new FullCacheTest(customDataComponents) {
      reset(emailConnector)

      val testSubId = "888777666"
      when(rosmService.performSubscription(any)(any))
        .thenReturn(Future.successful(Right(testSubId)))

      await(SUT.post(fakeRequest))

      verify(emailConnector).sendTemplatedEmail(
        emailAddress = ArgumentMatchers.eq(customTestEmail),
        templateName = ArgumentMatchers.eq("lisa_application_submit"),
        params = ArgumentMatchers.eq(Map(
          "application_reference" -> testSubId,
          "email" -> customTestEmail,
          "review_date" -> LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("d MMMM y")),
          "first_name" -> yourForm.firstName,
          "last_name" -> yourForm.lastName)))(any)
    }

    "handle a failed rosm registration" when {
      "the rosm service returns a failure" in new FullCacheTest(allDataComponents) {

        when(rosmService.performSubscription(any)(any))
          .thenReturn(Future.successful(Left("INTERNAL_SERVER_ERROR")))

        val result: Future[Result] = SUT.post(fakeRequest)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "audit a successful rosm registration" in new FullCacheTest(allDataComponents) {

      val registrationDetails: LisaRegistration = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm, "123456")

      when(rosmService.performSubscription(any)(any))
        .thenReturn(Future.successful(Right("123456789012")))

      await(SUT.post(fakeRequest))

      verify(auditService).audit(
        auditType = ArgumentMatchers.eq("applicationReceived"),
        path = ArgumentMatchers.eq("/lifetime-isa/submit-registration"),
        auditData = ArgumentMatchers.eq(Map(
          "subscriptionId" -> "123456789012",
          "companyName" -> registrationDetails.organisationDetails.companyName,
          "utr" -> registrationDetails.organisationDetails.ctrNumber,
          "financialServicesRegisterReferenceNumber" -> registrationDetails.tradingDetails.fsrRefNumber,
          "isaProviderReferenceNumber" -> registrationDetails.tradingDetails.isaProviderRefNumber,
          "firstName" -> registrationDetails.yourDetails.firstName,
          "lastName" -> registrationDetails.yourDetails.lastName,
          "roleInOrganisation" -> registrationDetails.yourDetails.role,
          "phoneNumber" -> registrationDetails.yourDetails.phone,
          "emailAddress" -> registrationDetails.yourDetails.email)))(any)
    }

    "audit a failed rosm registration" when {
      "the ct utr is 0000000000" in new FullCacheTest(allDataComponents) {
        val registrationDetails: LisaRegistration = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm, "123456")

        when(rosmService.performSubscription(any)(any))
          .thenReturn(Future.successful(Left("INVALID_LISA_MANAGER_REFERENCE_NUMBER")))

        await(SUT.post(fakeRequest))

        verify(auditService).audit(
          auditType = ArgumentMatchers.eq("applicationNotReceived"),
          path = ArgumentMatchers.eq("/lifetime-isa/submit-registration"),
          auditData = ArgumentMatchers.eq(Map(
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
        )(any)
      }
    }

    "cache subscriptionId and email as part of a successful rosm registration" in new FullCacheTest(allDataComponents){

      val registrationDetails: LisaRegistration = LisaRegistration(organisationForm, tradingForm, businessStructureForm, yourForm, "123456")

      when(rosmService.performSubscription(any)(any)).thenReturn(Future.successful(Right("123456789012")))

      await(SUT.post(fakeRequest))

      val applicationSentVM: ApplicationSent = ApplicationSent(subscriptionId = "123456789012", email = registrationDetails.yourDetails.email)

      verify(lisaCacheRepository).putSession(
        DataKey(ArgumentMatchers.eq(ApplicationSent.cacheKey)), ArgumentMatchers.eq(applicationSentVM))(
          any, any
        )
    }
  }
}
