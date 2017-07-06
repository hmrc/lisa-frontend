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

import helpers.CSRFTest
import models.{TaxEnrolmentDoesNotExist, TaxEnrolmentPending, UserAuthorised, UserDetails}
import org.mockito.Matchers.{any, matches}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ApplicationSubmittedControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Application Submitted" must {

    "return the submitted page with correct email address" in {

      when(mockAuthorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentPending)))

      val result = SUT.get("test@user.com")(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (submittedPageTitle)
      content must include ("test@user.com")

    }

  }

  "GET Application Pending" must {

    "return the pending page" in {

      when(mockAuthorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

      val result = SUT.pending()(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (pendingPageTitle)

    }

  }

  "GET Application Successful" must {

    "return the successful page" in {

      when(mockAuthorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

      val result = SUT.successful()(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (successPageTitle)
      
    }
    
  }

  "GET Application Rejected" must {

    "return the unsuccessful page" in {

      when(mockAuthorisationService.userStatus(any())).
        thenReturn(Future.successful(UserAuthorised("id", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

      val result = SUT.rejected()(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include (rejectedPageTitle)
      
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val submittedPageTitle = ">Application submitted</h1>"
  val pendingPageTitle = ">Application pending</h1>"
  val successPageTitle = ">Application successful</h1>"
  val rejectedPageTitle = ">Application not successful</h1>"
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  object SUT extends ApplicationSubmittedController {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
    override val authorisationService: AuthorisationService = mockAuthorisationService
  }

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}
