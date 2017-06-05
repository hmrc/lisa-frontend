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

package utils

import org.joda.time.DateTime
import utils.LisaExtensions._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class LisaExtensionsSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite {

  "LisaExtensions" must {

    // Test classes, not representative of any production code
    case class Submission(submittedDate: Option[DateTime] = None, submittedBy: Option[User] = None)
    case class User(name: String, age: Option[Int], heightM: Float, address: Address)
    case class Address(number: Int, postcode: String)

    "Return empty when no data" in {
      val submission = Submission()

      submission.toStringMap mustBe Map()
    }

    "Return datetime in the correct format" in {
      val submission = Submission(submittedDate = Some(new DateTime("2000-12-20")))

      submission.toStringMap mustBe Map("submittedDate" -> "2000-12-20")
    }

    "Return a flattened structure from nested classes" in {
      val address = Address(1, "AB1 1AB")
      val user = User("Joe", Some(35), 1.88f, address)
      val submission = Submission(submittedDate = Some(new DateTime("2000-12-20")), submittedBy = Some(user))

      submission.toStringMap mustBe Map(
        "submittedDate" -> "2000-12-20",
        "name" -> "Joe",
        "age" -> "35",
        "heightM" -> "1.88",
        "number" -> "1",
        "postcode" -> "AB1 1AB"
      )
    }

  }

}