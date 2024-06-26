/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsJson, MessagesControllerComponents, Request}
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeHeaders, FakeRequest, Injecting}
import uk.gov.hmrc.mongo.cache.DataKey
import views.html.registration.business_structure

import scala.concurrent.Future

class BusinessStructureControllerSpec extends SpecBase with Injecting {

  lazy val pageTitle = "What is your company structure?"

  def createFakePostRequest[T](uri: String, body:T): Request[T] = {
    val request:Request[T] = FakeRequest("POST", uri, FakeHeaders(), body)
    CSRFTokenHelper.addCSRFToken(request)
  }

  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit val businessStructureView: business_structure = inject[business_structure]
  lazy val SUT = new BusinessStructureController()


  "GET Business Structure" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val form = new BusinessStructure("LLP")

        when(lisaCacheRepository.getFromSession[BusinessStructure](DataKey(ArgumentMatchers.eq(BusinessStructure.cacheKey)))(
          ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(form)))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("checked")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(lisaCacheRepository.getFromSession[BusinessStructure](DataKey(ArgumentMatchers.eq(BusinessStructure.cacheKey)))(
          ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.OK

        val content = contentAsString(result)
        content must include (pageTitle)
        content must not include ("checked=\"checked\"")
      }

    }

  }

  "POST Business Structure" must {

    "return validation errors" when {
      "the submitted data is incomplete" in {
        val uri = controllers.routes.BusinessStructureController.post.url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("form-field--error") // css class indicating a form field error
        content must include ("Select if your company is a limited liability partnership, limited company or friendly society") // error msg we expect
      }
    }

    "redirect the user to organisation details" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.BusinessStructureController.post.url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyStructure" -> "LLP")))
        when(lisaCacheRepository.putSession[BusinessStructure](DataKey(ArgumentMatchers.eq(BusinessStructure.cacheKey)), ArgumentMatchers.any())(
          ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future.successful(("", "")))
        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get.url)
      }
    }

    "store business structure details in cache" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.BusinessStructureController.post.url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyStructure" -> "LLP")))

        await(SUT.post(request))

        verify(lisaCacheRepository).putSession[BusinessStructure](DataKey(ArgumentMatchers.eq(BusinessStructure.cacheKey)), ArgumentMatchers.any())(
          ArgumentMatchers.any(),ArgumentMatchers.any())
      }
    }

  }
}
