/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContentAsJson, MessagesControllerComponents, Request}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest, Injecting}
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.test.CSRFTokenHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TradingDetailsControllerSpec extends SpecBase with Injecting {

  "GET Trading Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val tradingForm = new TradingDetails(fsrRefNumber = "validFSRRefNumber", isaProviderRefNumber = "validISARefNumber")

        when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[TradingDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(TradingDetails.cacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(tradingForm)))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include (tradingForm.fsrRefNumber)
        content must include (tradingForm.isaProviderRefNumber)
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), ArgumentMatchers.eq(Reapplication.cacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(false)))

        when(shortLivedCache.fetchAndGetEntry[TradingDetails](ArgumentMatchers.any(), ArgumentMatchers.eq(TradingDetails.cacheKey))(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("value=\"\"")
      }

    }

  }

  "POST Trading Details" must {

    "return validation errors" when {
      "the submitted data is incomplete" in {
        val uri = controllers.routes.TradingDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include (pageTitle)
        content must include ("Enter your 6 number Financial Conduct Authority reference")
        content must include ("Enter your ISA manager reference")
      }
      "the submitted data is invalid - lowercase z" in {
        val uri = controllers.routes.TradingDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj( "fsrRefNumber" -> "654321",
          "isaProviderRefNumber" -> "z1234")))
        when(shortLivedCache.cache[TradingDetails](ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())(
          ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(new CacheMap("",Map[String,JsValue]())))
        val result = SUT.post(request)
        status(result) mustBe Status.BAD_REQUEST
      }
    }

    "redirect the user to your details" when {
      "the submitted data is valid - uppercase z" in {
        val uri = controllers.routes.TradingDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = validJsonUppercase))
        when(shortLivedCache.cache[TradingDetails](ArgumentMatchers.any(),ArgumentMatchers.any(),ArgumentMatchers.any())(
          ArgumentMatchers.any(),ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(new CacheMap("",Map[String,JsValue]())))
        val result = SUT.post(request)
        status(result) mustBe Status.SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.YourDetailsController.get().url)
      }
    }

    "store trading details in cache" when {
      "the submitted data is valid - uppercase z" in {
        val uri = controllers.routes.TradingDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = validJsonUppercase))
        await(SUT.post(request))
        verify(shortLivedCache).cache[TradingDetails](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(
          ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      }
    }

  }

  val pageTitle = "<h1>Your company’s reference numbers</h1>"

  val validJsonUppercase: JsObject = Json.obj(
    "fsrRefNumber" -> "654321",
    "isaProviderRefNumber" -> "Z1234"
  )

  def createFakePostRequest[T](uri: String, body:T): Request[T] = {
    FakeRequest("POST", uri, FakeHeaders(), body).withCSRFToken
  }
  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val SUT = new TradingDetailsController()

}
