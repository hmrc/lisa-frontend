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

import models._
import org.mockito.Matchers.{eq => MatcherEquals, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

import scala.concurrent.Future

class LisaBaseControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with BeforeAndAfter {

  "Lisa Base Controller" should {

    before {
      reset(mockSessionCache)
    }

    "redirect to login" when {

      "a not logged in response is returned from auth" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserNotLoggedIn))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        val redirectUrl = redirectLocation(result).getOrElse("")

        redirectUrl must startWith("/gg/sign-in?continue=%2Flifetime-isa")
        redirectUrl must endWith("&origin=lisa-frontend")
      }

    }

    "redirect to access denied" when {

      "a unauthorised response is returned from auth" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserUnauthorised))

        val result = SUT.testAuthorisation(fakeRequest)

        redirectLocation(result) mustBe Some(routes.ErrorController.accessDenied().url)
      }

    }

    "redirect to error page" when {

      "getting the user status fails" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.failed(new RuntimeException("No internalId for user")))

        val result = SUT.testAuthorisation(fakeRequest)

        redirectLocation(result) mustBe Some(routes.ErrorController.error().url)
      }

    }

    "redirect to pending subscription" when {

      "an authorised user has a pending subscription" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserAuthorised("", UserDetails(None, None, ""), TaxEnrolmentPending)))

        val result = SUT.testAuthorisation(fakeRequest)

        redirectLocation(result) mustBe Some(routes.ApplicationSubmittedController.pending().url)

      }

    }

    "redirect to rejected subscription" when {

      "an authorised user has a errored subscription" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserAuthorised("", UserDetails(None, None, ""), TaxEnrolmentError)))

        val result = SUT.testAuthorisation(fakeRequest)

        redirectLocation(result) mustBe Some(routes.ApplicationSubmittedController.rejected().url)

      }

    }

    "redirect to successful subscription" when {

      "an authorised user has a successful subscription" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserAuthorisedAndEnrolled("", UserDetails(None, None, ""), "Z9876")))

        val result = SUT.testAuthorisation(fakeRequest)

        redirectLocation(result) mustBe Some(routes.ApplicationSubmittedController.successful().url)

        verify(mockSessionCache).cache(MatcherEquals("lisaManagerReferenceNumber"), MatcherEquals("Z9876"))(any(), any())

      }

    }

    "avoid redirections" when {

      "enrolment state check is disabled for a successful subscription" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserAuthorisedAndEnrolled("12345", UserDetails(None, None, ""), "Z9876")))

        val result = SUT.testAuthorisationNoCheck(fakeRequest)

        status(result) mustBe Status.OK

        contentAsString(result) mustBe "Authorised. Cache ID: 12345-lisa-registration"
      }

      "enrolment state check is disabled for a pending subscription" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserAuthorised("12345", UserDetails(None, None, ""), TaxEnrolmentPending)))

        val result = SUT.testAuthorisationNoCheck(fakeRequest)

        status(result) mustBe Status.OK

        contentAsString(result) mustBe "Authorised. Cache ID: 12345-lisa-registration"
      }

    }

    "allow access" when {

      "an authorised user has no subscriptions in progress" in {
        when(mockAuthorisationService.userStatus(any())).
          thenReturn(Future.successful(UserAuthorised("12345", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.OK

        contentAsString(result) mustBe "Authorised. Cache ID: 12345-lisa-registration"
      }

    }

    "handle redirections" when {

      "there is no return url" in {
        val result = SUT.handleRedirect(routes.TradingDetailsController.get().url)(fakeRequest)

        redirectLocation(result) mustBe Some(routes.TradingDetailsController.get().url)
      }

      "there return url is a valid lisa url" in {
        val req = FakeRequest("GET", s"/?returnUrl=${routes.SummaryController.get().url}")
        val result = SUT.handleRedirect(routes.TradingDetailsController.get().url)(req)

        redirectLocation(result) mustBe Some(routes.SummaryController.get().url)
      }

      "the return url is an external url" in {
        val req = FakeRequest("GET", "/?returnUrl=http://news.ycombinator.com")
        val result = SUT.handleRedirect(routes.TradingDetailsController.get().url)(req)

        redirectLocation(result) mustBe Some(routes.TradingDetailsController.get().url)
      }

      "the return url is a protocol-relative external url" in {
        val req = FakeRequest("GET", "/?returnUrl=//news.ycombinator.com")
        val result = SUT.handleRedirect(routes.TradingDetailsController.get().url)(req)

        redirectLocation(result) mustBe Some(routes.TradingDetailsController.get().url)
      }

      "the return url is a relative url for a non-lisa service" in {
        val req = FakeRequest("GET", "/?returnUrl=/test")
        val result = SUT.handleRedirect(routes.TradingDetailsController.get().url)(req)

        redirectLocation(result) mustBe Some(routes.TradingDetailsController.get().url)
      }

    }

  }

  val fakeRequest = FakeRequest("GET", "/")

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  trait SUT extends LisaBaseController {
    val testAuthorisation: Action[AnyContent] = Action.async { implicit request =>
      authorisedForLisa(handleResult)
    }

    val testAuthorisationNoCheck: Action[AnyContent] = Action.async { implicit request =>
      authorisedForLisa(handleResult, checkEnrolmentState = false)
    }

    private val handleResult: (String) => Future[Result] = {
      case "error-lisa-registration" => throw new RuntimeException("An error occurred")
      case cacheId => Future.successful(Ok(s"Authorised. Cache ID: $cacheId"))
    }
  }

  object SUT extends SUT {
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val shortLivedCache: ShortLivedCache = mockCache
    override val sessionCache: SessionCache = mockSessionCache
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
