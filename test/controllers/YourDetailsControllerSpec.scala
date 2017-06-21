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
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.{Configuration, Environment, Mode}
import services.{AuthorisationService, TaxEnrolmentService}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class YourDetailsControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest
  with BeforeAndAfter {

  "GET Your Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val yourForm = new YourDetails(
          firstName = "Test",
          lastName = "User",
          role = "Role",
          phone = "0191 123 4567",
          email = "test@test.com")

        when(mockCache.fetchAndGetEntry[YourDetails](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(yourForm)))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Your details</h1>")
        content must include ("test@test.com")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(mockCache.fetchAndGetEntry[YourDetails](any(), any())(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Your details</h1>")
        content must include ("value=\"\"")
      }

    }

  }

  "POST Your Details" must {

    before {
      reset(mockCache)
    }

    "return validation errors" when {
      "the submitted data is incomplete" in {
        val uri = controllers.routes.YourDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include ("<h1>Your details</h1>")
        content must include ("This field is required")
      }
    }

    "redirect the user to your details" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.YourDetailsController.post().url
        val validJson = Json.obj(
          "firstName" -> "Test",
          "lastName" -> "User",
          "role" -> "Role",
          "phone" -> "0191 123 4567",
          "email" -> "test@test.com"
        )
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = validJson))
        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.SummaryController.get().url)
      }
    }

    "store your details in cache" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.YourDetailsController.post().url
        val validJson = Json.obj(
          "firstName" -> "Test",
          "lastName" -> "User",
          "role" -> "Role",
          "phone" -> "0191 123 4567",
          "email" -> "test@test.com"
        )
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = validJson))
        await(SUT.post(request))

        verify(mockCache).cache[YourDetails](any(), any(), any())(any(), any())
      }

    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  def createFakePostRequest[T](uri: String, body:T):FakeRequest[T] = {
    addToken(FakeRequest("POST", uri, FakeHeaders(), body))
  }

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]

  object SUT extends YourDetailsController {
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
