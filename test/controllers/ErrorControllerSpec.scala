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
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.api.test.Injecting

import scala.concurrent.ExecutionContext.Implicits.global

class ErrorControllerSpec extends SpecBase with Injecting {

  "Individual or Agent endpoint" should {
    "return a forbidden status page with correct messaging" in {
      val result = SUT.accessDeniedIndividualOrAgent(fakeRequest)
      status(result) mustBe Status.FORBIDDEN
      val content = contentAsString(result)

      content must include("There is a problem</h1>")
      content must include("You signed in as an individual or agent.")
    }
  }

  "Assistant endpoint" should {
    "return a forbidden status page with correct messaging" in {
      val result = SUT.accessDeniedAssistant(fakeRequest)
      status(result) mustBe Status.FORBIDDEN
      val content = contentAsString(result)

      content must include("There is a problem</h1>")
      content must include("You signed in as an assistant.")
    }
  }

  implicit val mcc = inject[MessagesControllerComponents]
  val SUT = new ErrorController()

}