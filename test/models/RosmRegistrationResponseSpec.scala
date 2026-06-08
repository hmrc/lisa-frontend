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

package models

import org.scalatestplus.play.PlaySpec

class RosmRegistrationResponseSpec extends PlaySpec {

  "RosmOrganisation" must {
    "apply with default values for optional fields" in {
      val org = RosmOrganisation(organisationName = "ACME Ltd")

      org.organisationName mustBe "ACME Ltd"
      org.isAGroup         mustBe None
      org.organisationType mustBe None
    }
  }

  "RosmAddress" must {
    "apply with default values for optional fields" in {
      val address = RosmAddress(addressLine1 = "1 Test Street", countryCode = "GB")

      address.addressLine1 mustBe "1 Test Street"
      address.addressLine2 mustBe None
      address.addressLine3 mustBe None
      address.addressLine4 mustBe None
      address.countryCode  mustBe "GB"
      address.postalCode   mustBe None
    }
  }

  "RosmContactDetails" must {
    "apply with all default values when no arguments are provided" in {
      val contactDetails = RosmContactDetails()

      contactDetails.primaryPhoneNumber   mustBe None
      contactDetails.secondaryPhoneNumber mustBe None
      contactDetails.faxNumber            mustBe None
      contactDetails.emailAddress         mustBe None
    }
  }

}
