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
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.{Configuration, Environment, Mode}
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

class HomePageControllerSpec extends PlaySpec with OneAppPerSuite with MockitoSugar {

  val fakeRequest = FakeRequest("GET", "/")
  
  "GET /" should {
    "301 redirect the user to the company structure page" in {
      val result = SUT.home(fakeRequest)
      status(result) mustBe Status.MOVED_PERMANENTLY
      redirectLocation(result).getOrElse("") mustBe "/lifetime-isa/company-structure"
    }
  }

  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]
  val mockSessionCache: SessionCache = mock[SessionCache]
  val mockAuthorisationService: AuthorisationService = mock[AuthorisationService]
  val mockAppConfig: AppConfig = mock[AppConfig]
  val mockMessages: Messages = mock[Messages]

  val SUT = new HomePageController(mockSessionCache, mockCache, mockEnvironment, mockConfig, mockAuthorisationService, mockAppConfig, mockMessages)

}
