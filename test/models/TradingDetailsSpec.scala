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

package models

import org.scalatestplus.play.PlaySpec
import play.api.data.FormError

class TradingDetailsSpec extends PlaySpec {

  "Trading Details form" must {

    "Show field required errors" when {

      "given no data" in {
        val test = Map[String, String]()
        val res = SUT.bind(test)

        res.errors mustBe Seq[FormError](FormError("fsrRefNumber", "error.required"), FormError("isaProviderRefNumber", "error.required"))
      }

    }

    "Show fsrRefNumber invalid error" when {

      "given a fsrRefNumber with invalid characters" in {
        val test = Map[String, String]("fsrRefNumber" -> "?", "isaProviderRefNumber" -> "Z123456")
        val res = SUT.bind(test)
        println(res.errors.toString)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "fsrRefNumber"
        res.errors.head.message mustBe "Enter a FCA number that is 6 characters long."
      }

    }

    "Show isa provider invalid error" when {

      "given a isa provider number with invalid characters" in {
        val test = Map[String, String]("fsrRefNumber" -> "123456", "isaProviderRefNumber" -> "?")
        val res = SUT.bind(test)
        println(res.errors.toString)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "isaProviderRefNumber"
        res.errors.head.message mustBe "Enter a valid ISA ref number. This starts with Z, and includes either 4 or 6 numbers."
      }

    }

  }

  val SUT = TradingDetails.form

}
