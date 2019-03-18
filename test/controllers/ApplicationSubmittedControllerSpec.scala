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
import org.mockito.Matchers.{eq => MatcherEquals, _}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfter
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.Injecting

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationSubmittedControllerSpec extends SpecBase
  with CSRFTest
  with BeforeAndAfter
  with Injecting {

  "GET Application Submitted" must {

    "return the submitted page with correct email address" in {

      when(authorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id",TaxEnrolmentPending)))

      when(sessionCache.fetchAndGetEntry[ApplicationSent](MatcherEquals(ApplicationSent.cacheKey))(any(), any(), any())).
        thenReturn(Future.successful(Some(ApplicationSent(email = "test@user.com", subscriptionId = "123456789"))))

      val result = SUT.get()(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (submittedPageTitle)
      content must include ("test@user.com")
      content must include ("123456789")

    }

  }

  "GET Application Pending" must {

    "return the pending page" in {

      when(authorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentDoesNotExist)))

      val result = SUT.pending()(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (pendingPageTitle)

    }

  }

  "GET Application Successful" must {

    "return the successful page" in {

      when(authorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentDoesNotExist)))

      when(sessionCache.fetchAndGetEntry[String](MatcherEquals("lisaManagerReferenceNumber"))(any(), any(), any())).
        thenReturn(Future.successful(Some("Z9999")))

      val result = SUT.successful()(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (successPageTitle)
      content must include ("Z9999")
      
    }
    
  }

  "GET Application Rejected" must {

    "return the unsuccessful page" in {

      when(authorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentDoesNotExist)))

      val result = SUT.rejected()(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (rejectedPageTitle)
      
    }

  }

  val submittedPageTitle = ">Application submitted</h1>"
  val pendingPageTitle = ">We are reviewing your application</h1>"
  val successPageTitle = ">Application successful</h1>"
  val rejectedPageTitle = ">Application not successful</h1>"
  implicit val mcc = inject[MessagesControllerComponents]
  val SUT = new ApplicationSubmittedController()

}
