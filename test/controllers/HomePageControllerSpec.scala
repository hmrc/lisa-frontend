/*
 * Copyright 2018 HM Revenue & Customs
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

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

class HomePageControllerSpec extends PlaySpec with OneAppPerSuite {

  val fakeRequest = FakeRequest("GET", "/")
  
  "GET /" should {
    "301 redirect the user to the company structure page" in {
      val result = HomePageController.home(fakeRequest)
      status(result) mustBe Status.MOVED_PERMANENTLY
      redirectLocation(result).getOrElse("") mustBe "/lifetime-isa/company-structure"
    }
  }

}
