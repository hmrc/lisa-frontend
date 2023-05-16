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

import models.RosmIndividual
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class RosmJsonFormatsSpec extends PlaySpec with RosmJsonFormats {

  "Rosm Individual" must {

    "serialize from json" when {
      "given valid json with a date of birth" in {
        val parsed = Json.parse(testJsonWithDob).as[RosmIndividual]

        parsed.dateOfBirth.isEmpty mustBe false

        val dob = parsed.dateOfBirth.get

        dob.getYear mustBe 2000
        dob.getMonthOfYear mustBe 1
        dob.getDayOfMonth mustBe 1
      }
      "given valid json without a date of birth" in {
        val parsed = Json.parse(testJsonWithoutDob).as[RosmIndividual]

        parsed.dateOfBirth.isEmpty mustBe true
      }
    }

    "serialize to json" when {
      "given valid individual with a date of birth" in {
        Json.toJson[RosmIndividual](testIndWithDob) mustBe Json.parse(testJsonWithDob)
      }
      "given valid individual without a date of birth" in {
        Json.toJson[RosmIndividual](testIndWithoutDob) mustBe Json.parse(testJsonWithoutDob)
      }
    }

  }

  private val testJsonWithDob: String = """{
                           |    "dateOfBirth": "2000-01-01",
                           |    "firstName": "Test",
                           |    "middleName": "A",
                           |    "lastName": "User"
                           |}""".stripMargin

  private val testJsonWithoutDob: String = testJsonWithDob.replace("\"dateOfBirth\": \"2000-01-01\",", "")

  private val testIndWithDob: RosmIndividual = RosmIndividual(
    firstName = "Test",
    middleName = Some("A"),
    lastName = "User",
    dateOfBirth = Some(new DateTime("2000-01-01"))
  )

  private val testIndWithoutDob: RosmIndividual = RosmIndividual(
    firstName = "Test",
    middleName = Some("A"),
    lastName = "User",
    dateOfBirth = None
  )

}
