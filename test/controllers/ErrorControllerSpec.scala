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

import java.io.File

import config.AppConfig
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment, Mode}
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

class ErrorControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  val fakeRequest = FakeRequest("GET", "/")

  "GET /access-denied" should {
    "return 403" in {
      val result = SUT.accessDenied(fakeRequest)
      status(result) mustBe Status.FORBIDDEN
      val content = contentAsString(result)

      content must include("There is a problem</h1>")
      content must include("You signed in as an individual or agent.")
    }
  }

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockMessages: Messages = mock[Messages]

  val SUT = new ErrorController(mockSessionCache, mockCache, mockEnvironment, mockConfig, mockAuthorisationService, mockAppConfig, mockMessages)

}