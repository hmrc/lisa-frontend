/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.mongo.cache.DataKey
import views.html.registration.matching_failed

import scala.concurrent.Future

class MatchingFailedControllerSpec extends SpecBase with Injecting {

  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit val matchingFailedView: matching_failed = inject[matching_failed]

  val SUT = new MatchingFailedController()

  "GET Matching Failed" must {

    "return a page" in {

      when(lisaCacheRepository.getFromSession[BusinessStructure](DataKey(ArgumentMatchers.eq(BusinessStructure.cacheKey)))(
        ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(new BusinessStructure("LLP"))))

      val result = SUT.get(fakeRequest)

      status(result) mustBe Status.OK

      val content = contentAsString(result)

      content must include ("Your company’s details could not be found</h1>")
    }

  }
}
