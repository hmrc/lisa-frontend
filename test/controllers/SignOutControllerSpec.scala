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
import play.api.http.Status
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import play.api.test.Injecting
import views.html.timeout_sign_out

import scala.concurrent.Future

class SignOutControllerSpec extends SpecBase with Injecting {
  implicit val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  implicit val timeoutView: timeout_sign_out = inject[timeout_sign_out]
  val SUT = new SignOutController()

  "Calling SignOutController.redirect" should {
    "respond with SEE OTHER" in {
      val result = SUT.redirect(fakeRequest)
      status(result) mustBe Status.SEE_OTHER
    }
  }

  "Calling SignOutController.timeout" should {
    val result: Future[Result] = SUT.timeout(fakeRequest)
    "respond with OK" in {
      status(result) mustBe Status.OK
    }
    "render the timeout page" in {
      def returnMessage(key: String): String = stubMessages(mcc.messagesApi).messages(key)

      val title: String = returnMessage("title.timeout-sign-out")
      val header: String = returnMessage("timeout.heading")
      val signInButton: String = returnMessage("timeout.sign-in-button")
      val pageAsString: String = contentAsString(result)

      pageAsString must include(title)
      pageAsString must include(header)
      pageAsString must include(signInButton)
    }
  }
}
