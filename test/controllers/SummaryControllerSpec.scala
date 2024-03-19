/*
 * Copyright 2023 HM Revenue & Customs
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

import org.apache.pekko.util.ByteString
import base.SpecBase
import helpers.FullCacheTest
import helpers.FullCacheTestData._
import play.api.http.Status
import play.api.libs.streams.Accumulator
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import play.api.test.Injecting
import play.api.test.CSRFTokenHelper._
import views.html.registration.summary

class SummaryControllerSpec extends SpecBase with Injecting {

  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit val summaryView: summary = inject[summary]

  val SUT = new SummaryController()

  "GET Summary" must {

    "redirect the user to business structure" when {
      "no business structure details are found in the cache" in new FullCacheTest(noBusinessStructureComponents) {

        val result: Accumulator[ByteString, Result] = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.BusinessStructureController.get.url)
      }
    }

    "redirect the user to organisation details" when {
      "no organisation details are found in the cache" in new FullCacheTest(noOrgDetailsComponents) {

        val result: Accumulator[ByteString, Result] = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get.url)
      }
      "no safeId is found in the cache" in new FullCacheTest(noSafeIdComponents) {

        val result: Accumulator[ByteString, Result] = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.OrganisationDetailsController.get.url)
      }
    }

    "redirect the user to trading details" when {
      "no trading details are found in the cache" in new FullCacheTest(noTradingDetailsComponents) {

        val result: Accumulator[ByteString, Result] = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get.url)
      }
    }

    "redirect the user to your details" when {
      "no your details are found in the cache" in new FullCacheTest(noFormDetailComponents) {

        val result: Accumulator[ByteString, Result] = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.YourDetailsController.get.url)
      }
    }

    "show the summary" when {
      "all required details are found in the cache" in new FullCacheTest(allDataComponents) {

        val result: Accumulator[ByteString, Result] = SUT.get(fakeRequest.withCSRFToken)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("Check your answers before sending your application</h1>")
        content must include ("test@test.com")
      }
    }

  }


}
