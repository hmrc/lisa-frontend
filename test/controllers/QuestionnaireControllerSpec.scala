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
import play.api.http.Status
import play.api.test.Helpers._

class QuestionnaireControllerSpec extends SpecBase {

  "Calling the QuestionnaireController.showQuestionnaire" should {
    "respond with OK" in {
      val result = SUT.showQuestionnaire(fakeRequest)
      status(result) mustBe Status.OK
    }
  }

  "Calling the QuestionnaireController.submitQuestionnaire" should {
    "respond with OK" in {
      val result = SUT.submitQuestionnaire(fakeRequest)
      status(result) mustBe Status.SEE_OTHER
    }
  }

  "Calling the QuestionnaireController.feedbackThankyou" should {
    "respond with OK" in {
      val result = SUT.feedbackThankyou(fakeRequest)
      status(result) mustBe Status.OK
    }
  }

  val SUT = new QuestionnaireController()

}
