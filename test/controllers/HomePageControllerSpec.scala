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
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.Injecting
import play.api.test.CSRFTokenHelper._

class HomePageControllerSpec extends SpecBase with Injecting {

  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val SUT = new HomePageController()

  "GET /" should {
    "301 redirect the user to the company structure page" in {
      val result = SUT.home(fakeRequest.withCSRFToken)
      status(result) mustBe Status.MOVED_PERMANENTLY
      redirectLocation(result).getOrElse("") mustBe "/lifetime-isa/company-structure"
    }
  }
}
