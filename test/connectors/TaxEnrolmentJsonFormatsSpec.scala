/*
 * Copyright 2023 HM Revenue & Customs
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

import models._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{Json, JsonValidationError}

import java.time.{Instant, LocalDateTime, ZoneId, ZonedDateTime}

class TaxEnrolmentJsonFormatsSpec extends PlaySpec with TaxEnrolmentJsonFormats {

  private val testJson: String = """[{
                                   |    "created": 1498726914908,
                                   |    "lastModified": 1498726914908,
                                   |    "serviceName": "HMRC-ORG-LISA",
                                   |    "identifiers": [
                                   |        {
                                   |            "key": "ZREF",
                                   |            "value": "Z1234"
                                   |        }
                                   |    ],
                                   |    "callback": "callback url",
                                   |    "etmpId": "bp safe id",
                                   |    "credId": "X",
                                   |    "state": "PENDING",
                                   |    "groupIdentifier": "Z"
                                   |}]""".stripMargin

  private val testJsonNullIdentifiers: String = """[{
                                                  |    "created": 1498726914908,
                                                  |    "lastModified": 1498726914908,
                                                  |    "serviceName": "HMRC-ORG-LISA",
                                                  |    "identifiers": null,
                                                  |    "callback": "callback url",
                                                  |    "etmpId": "bp safe id",
                                                  |    "credId": "X",
                                                  |    "state": "PENDING",
                                                  |    "groupIdentifier": "Z"
                                                  |}]""".stripMargin

  "Tax Enrolments" must {

    "serialize from json" when {
      "given valid json with a pending status" in {
        val parsed = Json.parse(testJson).as[List[TaxEnrolmentSubscription]]

        parsed.size mustBe 1

        val sub = parsed.head

        sub.created mustBe Instant.ofEpochMilli(1498726914908L).atZone(ZoneId.systemDefault())
        sub.state mustBe TaxEnrolmentPending
      }
      "given valid json with a error status" in {
        val parsed = Json.parse(testJson.replace("PENDING", "ERROR")).as[List[TaxEnrolmentSubscription]]

        parsed.size mustBe 1

        val sub = parsed.head

        sub.state mustBe TaxEnrolmentError
      }
      "given valid json with a success status" in {
        val parsed = Json.parse(testJson.replace("PENDING", "SUCCEEDED")).as[List[TaxEnrolmentSubscription]]

        parsed.size mustBe 1

        val sub = parsed.head

        sub.state mustBe TaxEnrolmentSuccess
      }
      "given valid json with identifiers set to null" in {
        val parsed = Json.parse(testJsonNullIdentifiers).as[List[TaxEnrolmentSubscription]]

        parsed.size mustBe 1

        val sub = parsed.head

        sub.state mustBe TaxEnrolmentPending
        sub.identifiers mustBe Nil
      }
    }

    "not serialize from json" when {
      "given an invalid state" in {
        val invalidStateJson = testJson.replace("PENDING", "TEST")

        val parsed = Json.parse(invalidStateJson).validate[List[TaxEnrolmentSubscription]]

        parsed.fold(invalid => {
          invalid.size mustBe 1
          invalid.head._2.size mustBe 1
          invalid.head._2.head mustBe JsonValidationError("error.formatting.state")
        },
          _ => {
            fail("passed validation")
          }
        )
      }
    }

  }

}
