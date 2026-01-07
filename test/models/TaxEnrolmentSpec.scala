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

import java.time.ZonedDateTime
import org.scalatestplus.play.PlaySpec

import java.time.temporal.ChronoUnit

class TaxEnrolmentSpec extends PlaySpec {

  "TaxEnrolmentSubscription zref" must {

    "return a None" when {

      "there are no identifiers" in {

        TaxEnrolmentSubscription(
          created = ZonedDateTime.now().plus(1, ChronoUnit.DAYS),
          lastModified = ZonedDateTime.now(),
          credId = "",
          serviceName = "HMRC-LISA-ORG",
          identifiers = Nil,
          callback = "",
          state = TaxEnrolmentError,
          etmpId = "",
          groupIdentifier = ""
        ).zref mustBe None

      }

    }

    "return the correct value" when {

      "there is a zref identifier on its own" in {

        TaxEnrolmentSubscription(
          created = ZonedDateTime.now().plus(1, ChronoUnit.DAYS),
          lastModified = ZonedDateTime.now(),
          credId = "",
          serviceName = "HMRC-LISA-ORG",
          identifiers = List(TaxEnrolmentIdentifier("ZREF", "Z1234")),
          callback = "",
          state = TaxEnrolmentError,
          etmpId = "",
          groupIdentifier = ""
        ).zref mustBe Some("Z1234")

      }

      "there is a zref identifier amongst other identifiers" in {

        TaxEnrolmentSubscription(
          created = ZonedDateTime.now().plus(1, ChronoUnit.DAYS),
          lastModified = ZonedDateTime.now(),
          credId = "",
          serviceName = "HMRC-LISA-ORG",
          identifiers = List(
            TaxEnrolmentIdentifier("AB", "12"),
            TaxEnrolmentIdentifier("CD", "34"),
            TaxEnrolmentIdentifier("ZREF", "Z1234")
          ),
          callback = "",
          state = TaxEnrolmentError,
          etmpId = "",
          groupIdentifier = ""
        ).zref mustBe Some("Z1234")

      }

    }

    "return the first zref" when {

      "there are multiple zref identifiers" in {

        TaxEnrolmentSubscription(
          created = ZonedDateTime.now().plus(1, ChronoUnit.DAYS),
          lastModified = ZonedDateTime.now(),
          credId = "",
          serviceName = "HMRC-LISA-ORG",
          identifiers = List(TaxEnrolmentIdentifier("ZREF", "Z5555"), TaxEnrolmentIdentifier("ZREF", "Z1234")),
          callback = "",
          state = TaxEnrolmentError,
          etmpId = "",
          groupIdentifier = ""
        ).zref mustBe Some("Z5555")

      }

    }

  }

}
