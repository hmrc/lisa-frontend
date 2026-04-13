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
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN}
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import play.api.test.Injecting
import views.html.{error_template, page_not_found_template}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ErrorHandlerSpec extends SpecBase with BeforeAndAfter with Injecting {
  val errorView: error_template = inject[error_template]

  val notFoundView: page_not_found_template = inject[page_not_found_template]

  private val errorHandler: ErrorHandler =
    new ErrorHandler(
      messagesApi = messagesApi,
      configuration = configuration,
      errorView = errorView,
      notFoundView = notFoundView
    )

  "ErrorHandler" must {

    "return an error page" in {
      val result = Await.result(
        errorHandler.standardErrorTemplate(pageTitle = "pageTitle", heading = "heading", message = "message")(using
          fakeRequest
        ),
        Duration.Inf
      )

      val pageTitle   = returnMessage("global.error.InternalServerError500.title")
      val pageHeading = returnMessage("global.error.InternalServerError500.heading")

      result.body must include(pageTitle)
      result.body must include(pageHeading)
    }

    "return a not found page" in {
      val result          = Await.result(
        errorHandler.notFoundTemplate(using
          fakeRequest
        ),
        Duration.Inf
      )
      val notFoundTitle   = returnMessage("global.page.not.found.error.title")
      val notFoundHeading = returnMessage("global.page.not.found.error.heading")

      result.body must include(notFoundTitle)
      result.body must include(notFoundHeading)
    }

    "return a forbidden page for onClient error" in {
      val result = errorHandler.onClientError(fakeRequest, FORBIDDEN, "Try again")

      val body = contentAsString(result)

      body               must include("Try again")
      status(result) shouldBe FORBIDDEN
    }

    "return a bad request page for onClient error" in {
      val result = errorHandler.onClientError(fakeRequest, BAD_REQUEST, "Bad Request")

      status(result) shouldBe BAD_REQUEST
    }

  }

}
