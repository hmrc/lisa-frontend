/*
 * Copyright 2026 HM Revenue & Customs
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
import models.*
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfter
import play.api.http.Status
import play.api.test.CSRFTokenHelper.*
import play.api.test.Helpers.*
import play.api.test.Injecting
import uk.gov.hmrc.mongo.cache.DataKey
import views.html.registration.{
  application_pending, application_rejected, application_submitted, application_successful
}

import scala.concurrent.Future

class ApplicationSubmittedControllerSpec extends SpecBase with BeforeAndAfter with Injecting {

  val submittedPageTitle                     = "Application submitted"
  val pendingPageTitle                       = "We are reviewing your application"
  val successPageTitle                       = "Application successful"
  val rejectedPageTitle                      = "Application not successful"
  val submittedView: application_submitted   = inject[application_submitted]
  val pendingView: application_pending       = inject[application_pending]
  val successfulView: application_successful = inject[application_successful]
  val rejectedView: application_rejected     = inject[application_rejected]

  val SUT = new ApplicationSubmittedController(
    sessionCacheRepository = lisaCacheRepository,
    env = env,
    config = configuration,
    authorisationService = authorisationService,
    messagesApi = messagesApi,
    mcc,
    applicationSubmittedView = submittedView,
    applicationPendingView = pendingView,
    applicationSuccessfulView = successfulView,
    applicationRejectedView = rejectedView
  )

  "GET Application Submitted" must {

    "return the submitted page with correct email address" in {

      when(authorisationService.userStatus(using any()))
        .thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentPending)))

      when(
        lisaCacheRepository.getFromSession[ApplicationSent](DataKey(ArgumentMatchers.eq(ApplicationSent.cacheKey)))(
          any(),
          any()
        )
      )
        .thenReturn(Future.successful(Some(ApplicationSent(email = "test@user.com", subscriptionId = "123456789"))))

      val result = SUT.get()(fakeRequest.withCSRFToken)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include(submittedPageTitle)
      content must include("test@user.com")
      content must include("123456789")

    }

    "redirect the user to business structure when no session found" in {

      when(
        lisaCacheRepository.getFromSession[ApplicationSent](DataKey(ArgumentMatchers.eq(ApplicationSent.cacheKey)))(
          any(),
          any()
        )
      )
        .thenReturn(Future.successful(None))

      val result = SUT.get()(fakeRequest.withCSRFToken)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get.url)

    }

  }

  "GET Application Pending" must {

    "return the pending page" in {

      when(authorisationService.userStatus(using any()))
        .thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentDoesNotExist)))

      val result = SUT.pending()(fakeRequest.withCSRFToken)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include(pendingPageTitle)

    }

  }

  "GET Application Successful" must {

    "return the successful page" in {

      when(authorisationService.userStatus(using any()))
        .thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentDoesNotExist)))

      when(
        lisaCacheRepository.getFromSession[String](DataKey(ArgumentMatchers.eq("lisaManagerReferenceNumber")))(
          any(),
          any()
        )
      )
        .thenReturn(Future.successful(Some("Z9999")))

      val result = SUT.successful()(fakeRequest.withCSRFToken)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include(successPageTitle)
      content must include("Z9999")

    }

    "redirect the user to business structure when no session found for Application Successful" in {

      when(
        lisaCacheRepository.getFromSession[String](DataKey(ArgumentMatchers.eq("lisaManagerReferenceNumber")))(
          any(),
          any()
        )
      )
        .thenReturn(Future.successful(None))

      val result = SUT.successful()(fakeRequest.withCSRFToken)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get.url)

    }
  }

  "GET Application Rejected" must {

    "return the unsuccessful page" in {

      when(authorisationService.userStatus(using any()))
        .thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentDoesNotExist)))

      val result = SUT.rejected()(fakeRequest.withCSRFToken)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include(rejectedPageTitle)

    }
  }

}
