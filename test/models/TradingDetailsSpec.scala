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

import org.scalatestplus.play.PlaySpec
import play.api.data.{Form, FormError}

class TradingDetailsSpec extends PlaySpec {

  "Trading Details form" must {

    "have no errors" when {

      "both fields are correct - lowercase z in isa ref" in {
        val test = Map[String, String]("fsrRefNumber" -> "654321", "isaProviderRefNumber" -> "z123456")
        val res = SUT.bind(test)
        res.errors mustBe Nil
      }

      "both fields are correct - uppercase z in isa ref" in {
        val test = Map[String, String]("fsrRefNumber" -> "654321", "isaProviderRefNumber" -> "Z123456")
        val res = SUT.bind(test)
        res.errors mustBe Nil
      }

    }

    "show field required errors" when {

      "given no data" in {
        val test = Map[String, String]()
        val res = SUT.bind(test)

        res.errors mustBe Seq[FormError](
          FormError("fsrRefNumber", "error.fsrRefNumberRequired"),
          FormError("isaProviderRefNumber", "error.isaProviderRefNumberRequired")
        )
      }

    }

    "show fsrRefNumber invalid error" when {

      "given a fsrRefNumber thats too long" in {
        val test = Map[String, String]("fsrRefNumber" -> "12", "isaProviderRefNumber" -> "Z123456")
        val res = SUT.bind(test)
        println(res.errors.toString)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "fsrRefNumber"
        res.errors.head.message mustBe "error.fsrRefNumberLength"
      }

      "given a fsrRefNumber with invalid characters" in {
        val test = Map[String, String]("fsrRefNumber" -> "?", "isaProviderRefNumber" -> "Z123456")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "fsrRefNumber"
        res.errors.head.message mustBe "error.fsrRefNumberPattern"
      }

    }

    "show isa provider invalid error" when {

      "given a isa provider number thats too long" in {
        val test = Map[String, String]("fsrRefNumber" -> "654321", "isaProviderRefNumber" -> "Z1234567890")
        val res = SUT.bind(test)

        res.errors.size mustBe 1
        res.errors.head.key mustBe "isaProviderRefNumber"
        res.errors.head.message mustBe "error.isaProviderRefNumberPattern"
      }

      "given a isa provider number with invalid characters" in {
        val test = Map[String, String]("fsrRefNumber" -> "654321", "isaProviderRefNumber" -> "?")
        val res = SUT.bind(test)

        res.errors.size mustBe 1
        res.errors.head.key mustBe "isaProviderRefNumber"
        res.errors.head.message mustBe "error.isaProviderRefNumberPattern"
      }

    }

  }

  val SUT: Form[TradingDetails] = TradingDetails.form

}
