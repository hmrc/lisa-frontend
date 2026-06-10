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

package config

import base.SpecBase

import java.net.URLEncoder

class AppConfigSpec extends SpecBase {

  "AppConfig" must {

    "expose the lisa service base URL" in {
      appConfig.lisaServiceUrl mustBe "http://localhost:8886"
    }

    "expose the email service base URL" in {
      appConfig.emailServiceUrl mustBe "http://localhost:8300"
    }

    "expose the application name" in {
      appConfig.appName mustBe "lisa-frontend"
    }

    "expose the LISA API documentation URL" in {
      appConfig.apiUrl must include("/lisa-api/")
    }

    "expose the feedback redirect URL" in {
      appConfig.feedbackRedirectUrl must include("/feedback/lifetime-isa")
    }

    "expose the register organisation URL" in {
      appConfig.registerOrgUrl must include("RegisterOrLogon.aspx")
    }

    "expose authenticated and unauthenticated beta feedback URLs" in {
      appConfig.betaFeedbackUrl                must endWith("/contact/beta-feedback")
      appConfig.betaFeedbackUnauthenticatedUrl must endWith("/contact/beta-feedback-unauthenticated")
    }

    "expose the login callback URL" in {
      appConfig.loginCallback must include("/lifetime-isa/company-structure")
    }

    "expose the login URL" in {
      appConfig.loginURL must endWith("/bas-gateway/sign-in")
    }

    "expose the user research banner toggle" in {
      appConfig.displayURBanner mustBe true
    }

    "build sign-out and time-out URLs by encoding the callback URL" in {
      val callback        = "http://localhost:8884/lifetime-isa/signed-out"
      val encodedCallback = URLEncoder.encode(callback, "UTF-8")

      appConfig.signOutUrl must include(s"sign-out-without-state/?continue=$encodedCallback")
      appConfig.signOutUrl must startWith("http://localhost:9553/bas-gateway/")
      appConfig.timeOutUrl must include("sign-out-without-state/?continue=")
      appConfig.timeOutUrl must include(URLEncoder.encode("/lifetime-isa/timed-out", "UTF-8"))
    }

  }

}
