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
import org.mockito.Matchers._
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


class BusinessStructureControllerSpec extends SpecBase with CSRFTest with Injecting {

  "GET Business Structure" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val form = new BusinessStructure("LLP")

        when(shortLivedCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(BusinessStructure.cacheKey))(any(), any(), any())).
          thenReturn(Future.successful(Some(form)))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("checked=\"checked\"")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(shortLivedCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[BusinessStructure](any(), org.mockito.Matchers.eq(BusinessStructure.cacheKey))(any(), any(), any())).
          thenReturn(Future.successful(None))

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
        val uri = controllers.routes.BusinessStructureController.post().url
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
        val uri = controllers.routes.BusinessStructureController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyStructure" -> "LLP")))
        when(shortLivedCache.cache[BusinessStructure](any(),any(),any())(any(),any(), any())).thenReturn(Future.successful(new CacheMap("",Map[String,JsValue]())))
        val result = SUT.post(request)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get().url)
      }
    }

    "store business structure details in cache" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.BusinessStructureController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyStructure" -> "LLP")))

        await(SUT.post(request))

        verify(shortLivedCache).cache[BusinessStructure](any(), any(), any())(any(), any(), any())
      }
    }

  }

  val pageTitle = ">Select your company structure</h1>"

  def createFakePostRequest[T](uri: String, body:T): Request[T] = {
    FakeRequest("POST", uri, FakeHeaders(), body).withCSRFToken
  }
  implicit val mcc = inject[MessagesControllerComponents]
  val SUT = new BusinessStructureController()

}