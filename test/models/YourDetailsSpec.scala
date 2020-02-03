/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import org.scalatest.FunSuite
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError

class YourDetailsSpec extends PlaySpec {

  "Your Details form" must {

    "show field required errors" when {

      "given no data" in {
        val test = Map[String, String]()
        val res = SUT.bind(test)

        res.errors mustBe Seq[FormError](
          FormError("firstName", "error.firstNameRequired"),
          FormError("lastName", "error.lastNameRequired"),
          FormError("role", "error.roleRequired"),
          FormError("phone", "error.phoneRequired"),
          FormError("email", "error.emailRequired")
        )
      }

    }

    "show first name too long" when {

      "given a first name with too many characters" in {
        val tooLong = "!23456789012345678901234567890123456"
        val test = Map[String, String]("firstName"->tooLong, "lastName"->"B", "role"->"Manager", "phone"->"01234", "email"->"me@test.com")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "firstName"
        res.errors.head.message mustBe "error.firstNameLength"
      }

    }

    "show first name invalid error" when {

      "given a first name with invalid characters" in {
        val test = Map[String, String]("firstName" -> "?", "lastName"->"B", "role"->"Manager", "phone"->"01234", "email"->"me@test.com")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "firstName"
        res.errors.head.message mustBe "error.firstNamePattern"
      }

    }

    "show last name too long" when {

      "given a last name with too many characters" in {
        val tooLong = "!23456789012345678901234567890123456"
        val test = Map[String, String]("firstName"->"A", "lastName"->tooLong, "role"->"Manager", "phone"->"01234", "email"->"me@test.com")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "lastName"
        res.errors.head.message mustBe "error.lastNameLength"
      }

    }

    "show last name invalid error" when {

      "given a last name with invalid characters" in {
        val test = Map[String, String]("firstName" -> "A", "lastName"->"?", "role"->"Manager", "phone"->"01234", "email"->"me@test.com")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "lastName"
        res.errors.head.message mustBe "error.lastNamePattern"
      }

    }

    "show role too long" when {

      "given a role with too many characters" in {
        val tooLong = "!234567890123456789012345678901"
        val test = Map[String, String]("firstName"->"A", "lastName"->"B", "role"->tooLong, "phone"->"01234", "email"->"me@test.com")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "role"
        res.errors.head.message mustBe "error.roleLength"
      }

    }

    "show role invalid error" when {

      "given a role with invalid characters" in {
        val test = Map[String, String]("firstName" -> "A", "lastName"->"B", "role"->"?", "phone"->"01234", "email"->"me@test.com")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "role"
        res.errors.head.message mustBe "error.rolePattern"
      }

    }

    "show phone invalid error" when {

      "given a phone with invalid characters" in {
        val test = Map[String, String]("firstName" -> "A", "lastName"->"B", "role"->"Manager", "phone"->"?", "email"->"me@test.com")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "phone"
        res.errors.head.message mustBe "error.phonePattern"
      }

    }

    "show email invalid error" when {

      "given a email with invalid characters" in {
        val test = Map[String, String]("firstName" -> "A", "lastName"->"B", "role"->"Manager", "phone"->"01234", "email"->"?")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "email"
        res.errors.head.message mustBe "error.email"
      }

    }

  }

  val SUT = YourDetails.form

}
