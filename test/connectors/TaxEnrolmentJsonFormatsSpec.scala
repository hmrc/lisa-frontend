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

package connectors

import models.{TaxEnrolmentPending, TaxEnrolmentSubscription}
import org.scalatestplus.play.PlaySpec
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, Json}

class TaxEnrolmentJsonFormatsSpec extends PlaySpec with TaxEnrolmentJsonFormats {

  "Tax Enrolments" must {

    "serialize from json" when {
      "given valid json" in {
        val testJson = """[{
                         |    "created": "2016-01-25T15:44:17.496Z",
                         |    "lastModified": "2016-01-25T16:44:17.496Z",
                         |    "serviceName": "HMRC-ORG-LISA",
                         |    "identifiers": [
                         |        {
                         |            "key": "eori",
                         |            "value": "gb123456ghj"
                         |        }
                         |    ],
                         |    "callback": "callback url",
                         |    "etmpId": "bp safe id",
                         |
                         |    "credId": "X",
                         |    "state": "PENDING",
                         |    "groupIdentifier": "Z"
                         |}]""".stripMargin

        val parsed = Json.parse(testJson).as[List[TaxEnrolmentSubscription]]

        parsed.size mustBe 1

        val sub = parsed.head

        sub.created.toString mustBe "2016-01-25T15:44:17.496Z"
        sub.state mustBe TaxEnrolmentPending
      }
    }

    "not serialize from json" when {
      "given an invalid state" in {
        val invalidStateJson = """[{
                         |    "created": "2016-01-25T15:44:17.496Z",
                         |    "lastModified": "2016-01-25T16:44:17.496Z",
                         |    "serviceName": "HMRC-ORG-LISA",
                         |    "identifiers": [
                         |        {
                         |            "key": "eori",
                         |            "value": "gb123456ghj"
                         |        }
                         |    ],
                         |    "callback": "callback url",
                         |    "etmpId": "bp safe id",
                         |
                         |    "credId": "X",
                         |    "state": "Y",
                         |    "groupIdentifier": "Z"
                         |}]""".stripMargin

        val parsed = Json.parse(invalidStateJson).validate[List[TaxEnrolmentSubscription]]

        parsed.fold(
          (invalid: Seq[(JsPath, Seq[ValidationError])]) => {
            invalid.size mustBe 1
            invalid.head._2.size mustBe 1
            invalid.head._2.head mustBe ValidationError("error.formatting.state")
          },
          (_) => {
            fail("passed validation")
          }
        )
      }
    }

  }

}