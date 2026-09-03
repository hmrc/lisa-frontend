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

package views

import base.SpecBase
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.api.test.Injecting
import play.twirl.api.Html

class GovukWrapperSpec extends SpecBase with Injecting {

  private val govukWrapper: views.html.govuk_wrapper = inject[views.html.govuk_wrapper]

  private given request: RequestHeader = fakeRequest
  private given messages: Messages     = messagesApi.preferred(fakeRequest)

  private val page: String = govukWrapper(title = "Test page")(Html("<p>Test content</p>")).body
    .replace("&amp;", "&")

  private def hrefContaining(fragment: String): String =
    """href="([^"]+)"""".r
      .findAllMatchIn(page)
      .map(_.group(1))
      .find(_.contains(fragment))
      .getOrElse(fail(s"no link to $fragment was rendered"))

  "govuk_wrapper" must {

    "display the service name using the service navigation component" in {
      page must include("govuk-service-navigation")
      page must include(returnMessage("service.name"))
    }

    "link to the accessibility statement with the service navigation parameter" in {
      hrefContaining("/accessibility-statement/lifetime-isa") must endWith("&useServiceNavigation")
    }

    "link to the report a technical problem page with the service navigation parameter" in {
      val href = hrefContaining("/contact/report-technical-problem")

      href must include("service=LISA")
      href must endWith("&useServiceNavigation")
    }

    "link to the shared cookies, privacy and terms and conditions pages with the service navigation parameter" in {
      hrefContaining("/help/cookies")              must endWith("?useServiceNavigation")
      hrefContaining("/help/privacy")              must endWith("?useServiceNavigation")
      hrefContaining("/help/terms-and-conditions") must endWith("?useServiceNavigation")
    }

  }

}
