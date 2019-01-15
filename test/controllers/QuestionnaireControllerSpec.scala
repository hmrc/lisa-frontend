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

import org.mockito.Matchers.{eq => matcherEq}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier

class QuestionnaireControllerSpec extends PlaySpec with MockitoSugar with OneAppPerSuite {

  "Calling the QuestionnaireController.showQuestionnaire" should {
    "respond with OK" in {
      val result = SUT.showQuestionnaire(fakeRequest)
      status(result) mustBe Status.OK
    }
  }

  "Calling the QuestionnaireController.submitQuestionnaire" should {
    "respond with OK" in {
      val result = SUT.submitQuestionnaire(fakePostRequest)
      status(result) mustBe Status.SEE_OTHER
    }
  }
  "Calling the QuestionnaireController.feedbackThankyou" should {
    "respond with OK" in {
      val result = SUT.feedbackThankyou(fakeRequest)
      status(result) mustBe Status.OK
    }
  }
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val fakeRequest = FakeRequest("GET", "/")
  val fakePostRequest = FakeRequest("POST", "/signed-out")

  val mockAuditService: AuditService = mock[AuditService]

  object SUT extends QuestionnaireController {
    override val auditService = mockAuditService
  }

}
