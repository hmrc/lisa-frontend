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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import helpers.ConnectorSpecHelper
import play.api.libs.json.Json
import play.api.test.Helpers.*

class EmailConnectorSpec extends ConnectorSpecHelper {

  lazy val emailConnector: EmailConnector = injector.instanceOf[EmailConnector]

  private val emailUrl   = "/hmrc/email"
  private val templateId = "lisa_application_submit"
  private val params     = Map("testParam" -> "testParam")

  "EmailConnector" must {

    "return EmailSent" when {
      "the email service returns 202 ACCEPTED" in {
        val emailString = "test@mail.com"
        stubForPost(emailUrl, ACCEPTED)

        await(emailConnector.sendTemplatedEmail(emailString, templateId, params)) mustBe EmailSent

        server.verify(
          postRequestedFor(urlEqualTo(emailUrl))
            .withRequestBody(
              equalToJson(
                Json
                  .obj(
                    "to"         -> Json.arr(emailString),
                    "templateId" -> templateId,
                    "parameters" -> params,
                    "force"      -> true
                  )
                  .toString
              )
            )
        )
      }
    }

    "return EmailNotSent" when {
      "the email service returns any other status" in {
        stubForPost(emailUrl, NOT_FOUND)

        await(emailConnector.sendTemplatedEmail("test@test1.com", templateId, params)) mustBe EmailNotSent
      }
    }

  }

}
