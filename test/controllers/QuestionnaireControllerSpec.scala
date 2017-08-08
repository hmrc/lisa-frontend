/*
 * Copyright 2017 HM Revenue & Customs
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
import org.mockito.Matchers.{eq => matcherEq, _}
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.test.Helpers._
import play.api.http.Status
import org.scalatest.mockito.MockitoSugar

class QuestionnaireControllerSpec extends PlaySpec with MockitoSugar  with OneAppPerSuite {

  "Calling the QuestionnaireController.showQuestionnaire" should {
    "respond with OK" in {
      val result = SUT.showQuestionnaire(fakeRequest)
      status(result) mustBe Status.OK
    }
  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val fakeRequest = FakeRequest("GET", "/")

  object SUT extends QuestionnaireController {

  }

}
