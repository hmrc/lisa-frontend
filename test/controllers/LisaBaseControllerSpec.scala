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

import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core._

import scala.concurrent.Future

class LisaBaseControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar {

  "Lisa Base Controller" should {

    "redirect to login" when {

      "a missing bearer token response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new MissingBearerToken()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        val redirectUrl = redirectLocation(result).getOrElse("")

        redirectUrl must startWith("/gg/sign-in?continue=%2Flifetime-isa")
        redirectUrl must endWith("&origin=lisa-frontend")
      }

      "a invalid bearer token response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new InvalidBearerToken()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        val redirectUrl = redirectLocation(result).getOrElse("")

        redirectUrl must startWith("/gg/sign-in?continue=%2Flifetime-isa")
        redirectUrl must endWith("&origin=lisa-frontend")
      }

      "a bearer token expired response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new BearerTokenExpired()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        val redirectUrl = redirectLocation(result).getOrElse("")

        redirectUrl must startWith("/gg/sign-in?continue=%2Flifetime-isa")
        redirectUrl must endWith("&origin=lisa-frontend")
      }

      "a session record not found response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new SessionRecordNotFound()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.SEE_OTHER

        val redirectUrl = redirectLocation(result).getOrElse("")

        redirectUrl must startWith("/gg/sign-in?continue=%2Flifetime-isa")
        redirectUrl must endWith("&origin=lisa-frontend")
      }

    }

    "return access denied" when {

      "a insufficient confidence level response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new InsufficientConfidenceLevel()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.FORBIDDEN

        contentAsString(result) must include(accessDeniedHtmlFragment)
      }

      "a insufficient enrolments response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new InsufficientEnrolments()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.FORBIDDEN

        contentAsString(result) must include(accessDeniedHtmlFragment)
      }

      "a internal error response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new InternalError("auth unavailable")))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.FORBIDDEN

        contentAsString(result) must include(accessDeniedHtmlFragment)
      }

      "a unsupported affinity group response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new UnsupportedAffinityGroup()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.FORBIDDEN

        contentAsString(result) must include(accessDeniedHtmlFragment)
      }

      "a unsupported auth provider response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new UnsupportedAuthProvider()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.FORBIDDEN

        contentAsString(result) must include(accessDeniedHtmlFragment)
      }

      "a unsupported credential role response is returned from auth" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.failed(new UnsupportedCredentialRole()))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.FORBIDDEN

        contentAsString(result) must include(accessDeniedHtmlFragment)
      }

    }

    "return the error page" when {

      "the internal id retrieval fails" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.successful(None))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.INTERNAL_SERVER_ERROR

        contentAsString(result) must include(errorHtmlFragment)
      }

      "an error occurs within the controller body" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.successful(Some("error")))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.INTERNAL_SERVER_ERROR

        contentAsString(result) must include(errorHtmlFragment)
      }

    }

    "allow access" when {

      "authorisation passes" in {
        when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
          thenReturn(Future.successful(Some("12345")))

        val result = SUT.testAuthorisation(fakeRequest)

        status(result) mustBe Status.OK

        contentAsString(result) mustBe "Authorised. Cache ID: 12345-lisa-registration"
      }

    }

  }

  val fakeRequest = FakeRequest("GET", "/")

  val mockAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)

  val accessDeniedHtmlFragment = "<h1>Access Denied</h1>"
  val errorHtmlFragment = "<h1>An error occurred</h1>"

  trait SUT extends LisaBaseController {
    val testAuthorisation: Action[AnyContent] = Action.async { implicit request =>
      authorisedForLisa {
        case "error-lisa-registration" => throw new RuntimeException("An error occurred")
        case cacheId => Future.successful(Ok(s"Authorised. Cache ID: $cacheId"))
      }
    }
  }

  object SUT extends SUT {
    override val authConnector: PlayAuthConnector = mockAuthConnector
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
  }

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}
