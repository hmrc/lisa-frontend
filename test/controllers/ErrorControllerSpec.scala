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
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Injecting}
import views.html.error.{access_denied_assistant, access_denied_individual_or_agent}

class ErrorControllerSpec extends SpecBase with Injecting {

  val accessDeniedIndividualOrAgentView: access_denied_individual_or_agent =
    inject[access_denied_individual_or_agent]

  val accessDeniedAssistantView: access_denied_assistant = inject[access_denied_assistant]

  lazy val SUT = new ErrorController(
    config = configuration,
    env = env,
    messagesApi = messagesApi,
    messagesControllerComponents = mcc,
    accessDeniedIndividualOrAgentView = accessDeniedIndividualOrAgentView,
    accessDeniedAssistantView = accessDeniedAssistantView
  )

  "Individual or Agent endpoint" should {
    "return a forbidden status page with correct messaging" in {
      val result = SUT.accessDeniedIndividualOrAgent.apply(fakeRequest)
      status(result) mustBe Status.FORBIDDEN
      val content = contentAsString(result)

      content must include("There is a problem</h1>")
      content must include("You signed in as an individual or agent.")
    }
  }

  "Assistant endpoint" should {
    "return a forbidden status page with correct messaging" in {
      val result = SUT.accessDeniedAssistant.apply(fakeRequest)
      status(result) mustBe Status.FORBIDDEN
      val content = contentAsString(result)

      content must include("There is a problem</h1>")
      content must include("You signed in as an assistant.")
    }
  }

}
