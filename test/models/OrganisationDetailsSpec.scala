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

class OrganisationDetailsSpec extends PlaySpec {

  "Standard Organisation Details form" must {

    "show field required errors" when {

      "given no data" in {
        val test = Map[String, String]()
        val res = SUT.bind(test)

        res.errors mustBe Seq[FormError](FormError("companyName", "error.companyNameRequired"), FormError("ctrNumber", "error.ctrNumberRequired"))
      }

    }

    "show company name invalid error" when {

      "given a empty company name" in {
        val test = Map[String, String]("ctrNumber" -> "0123456789")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "companyName"
        res.errors.head.message mustBe "error.companyNameRequired"
      }

      "given a company name that's too long" in {
        val tooLong = "!234567890123456789012345678901234567890123456789012345678901234567"
        val test = Map[String, String]("companyName" -> tooLong, "ctrNumber" -> "0123456789")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "companyName"
        res.errors.head.message mustBe "error.companyNameLength"
      }

      "given a company name with invalid characters" in {
        val test = Map[String, String]("companyName" -> "?", "ctrNumber" -> "0123456789")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "companyName"
        res.errors.head.message mustBe "error.companyNamePattern"
      }

    }

    "show utr invalid error" when {

      "given a empty utr" in {
        val test = Map[String, String]("companyName" -> "ACME Ltd")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "ctrNumber"
        res.errors.head.message mustBe "error.ctrNumberRequired"
      }

      "given a utr that doesn't match the expected format" in {
        val test = Map[String, String]("companyName" -> "ACME Ltd", "ctrNumber" -> "?")
        val res = SUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "ctrNumber"
        res.errors.head.message mustBe "error.ctrNumberPattern"
      }

    }

  }

  "Partnership Organisation Details form" must {

    "show field required errors" when {

      "given no data" in {
        val test = Map[String, String]()
        val res = PartnershipSUT.bind(test)

        res.errors mustBe Seq[FormError](FormError("companyName", "error.companyNameRequired"), FormError("strNumber", "error.partnershipUtrRequired"))
      }

    }

    "show company name invalid error" when {

      "given a empty company name" in {
        val test = Map[String, String]("strNumber" -> "0123456789")
        val res = PartnershipSUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "companyName"
        res.errors.head.message mustBe "error.companyNameRequired"
      }

      "given a company name that's too long" in {
        val tooLong = "!234567890123456789012345678901234567890123456789012345678901234567"
        val test = Map[String, String]("companyName" -> tooLong, "strNumber" -> "0123456789")
        val res = PartnershipSUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "companyName"
        res.errors.head.message mustBe "error.companyNameLength"
      }

      "given a company name with invalid characters" in {
        val test = Map[String, String]("companyName" -> "?", "strNumber" -> "0123456789")
        val res = PartnershipSUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "companyName"
        res.errors.head.message mustBe "error.companyNamePattern"
      }

    }

    "show utr invalid error" when {

      "given a empty utr" in {
        val test = Map[String, String]("companyName" -> "ACME Ltd")
        val res = PartnershipSUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "strNumber"
        res.errors.head.message mustBe "error.partnershipUtrRequired"
      }

      "given a utr that doesn't match the expected format" in {
        val test = Map[String, String]("companyName" -> "ACME Ltd", "strNumber" -> "?")
        val res = PartnershipSUT.bind(test)
        res.errors.size mustBe 1
        res.errors.head.key mustBe "strNumber"
        res.errors.head.message mustBe "error.partnershipUtrPattern"
      }

    }

  }

  val SUT: Form[OrganisationDetails] = OrganisationDetails.form
  val PartnershipSUT: Form[OrganisationDetails] = OrganisationDetails.partnershipForm

}
