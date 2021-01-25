/*
 * Copyright 2021 HM Revenue & Customs
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
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, MessagesControllerComponents, Request}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest, Injecting}
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.test.CSRFTokenHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class YourDetailsControllerSpec extends SpecBase with Injecting {

  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val SUT = new YourDetailsController()

  def createFakePostRequest[T](uri: String, body:T): Request[T] = {
    FakeRequest("POST", uri, FakeHeaders(), body).withCSRFToken
  }

  "GET Your Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val yourForm = new YourDetails(
          firstName = "Test",
          lastName = "User",
          role = "Role",
          phone = "0191 123 4567",
          email = "test@test.com")

        when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))
          (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[YourDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(YourDetails.cacheKey))
          (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(yourForm)))

        val request = fakeRequest.withCSRFToken
        val result = SUT.get().apply(request)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Your name and contact details</h1>")
        content must include ("test@test.com")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))
          (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[YourDetails](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val request = fakeRequest.withCSRFToken
        val result = SUT.get().apply(request)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Your name and contact details</h1>")
        content must include ("value=\"\"")
      }

    }

  }

  "POST Your Details" must {

    "return validation errors" when {
      "the submitted data is incomplete" in {
        val uri = controllers.routes.YourDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include ("<h1>Your name and contact details</h1>")
        content must include ("Enter your first name")
        content must include ("Enter your last name")
        content must include ("Enter your job title")
        content must include ("Enter your phone number")
        content must include ("Enter your email address")
      }
    }

    "return validation errors" when {
      "the submitted data is incorrectly filled" in {
        val uri = controllers.routes.YourDetailsController.post().url
        val invalidJson = Json.obj(
          "firstName" -> "Test0",
          "lastName" -> "User&",
          "role" -> "Role.",
          "phone" -> "0191 123 4567a",
          "email" -> "test@eldf"
        )
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = invalidJson))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include ("<h1>Your name and contact details</h1>")
        content must include ("First name must only include letters a to z, hyphens, spaces and apostrophes")
        content must include ("Last name must only include letters a to z, hyphens, spaces and apostrophes")
        content must include ("Job title must only include letters a to z, hyphens, spaces and apostrophes")
        content must include ("Enter a phone number, like 01642 123 456 or +33 1 23 45 67 88")
        content must include ("Enter an email address with a name, @ symbol and a domain name, like yourname@example.com")
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
        when(shortLivedCache.cache[YourDetails](ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())
          (ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(new CacheMap("" , Map[String,JsValue]())))
        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.SummaryController.get().url)
      }
    }

    "store your details in cache" when {
      "the submitted data is valid" in {
        when(authorisationService.userStatus(ArgumentMatchers.any()))
          .thenReturn(Future.successful(UserAuthorised("id", TaxEnrolmentDoesNotExist)))

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

        verify(shortLivedCache).cache[YourDetails](ArgumentMatchers.any(),  ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())


      }

    }

  }

}
