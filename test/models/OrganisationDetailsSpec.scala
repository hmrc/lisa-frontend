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

class OrganisationDetailsSpec extends PlaySpec {

  "Organisation Details form" must {

    "show field required errors" when {

      "given no data" in {
        val test = Map[String, String]()
        val res = SUT.bind(test)

        res.errors mustBe Seq[FormError](FormError("companyName", "error.required"), FormError("ctrNumber", "error.required"))
      }

    }

    "show company name invalid error" when {

      "given a company name with invalid characters" in {
        val test = Map[String, String]("companyName" -> "?", "ctrNumber" -> "0123456789")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "companyName"
        res.errors.head.message mustBe "error.companyName"
      }

    }

    "show utr invalid error" when {

      "given a utr with invalid characters" in {
        val test = Map[String, String]("companyName" -> "ACME Ltd", "ctrNumber" -> "?")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "ctrNumber"
        res.errors.head.message mustBe "error.ctrNumber"
      }

    }

  }

  val SUT = OrganisationDetails.form

}
