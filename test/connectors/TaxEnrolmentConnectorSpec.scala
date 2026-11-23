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

package connectors

import helpers.ConnectorSpecHelper
import models.*
import play.api.libs.json.*
import play.api.test.Helpers.*

import java.time.{Instant, ZoneId, ZonedDateTime}

class TaxEnrolmentConnectorSpec extends ConnectorSpecHelper with TaxEnrolmentJsonFormats {

  lazy val taxEnrolmentConnector: TaxEnrolmentConnector = injector.instanceOf[TaxEnrolmentConnector]

  private val subscriptionsUrl = "/lisa/tax-enrolments/groups/1234567890/subscriptions"

  val instant: Instant             = Instant.ofEpochMilli(1498726914908L)
  val zonedDateTime: ZonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

  private val lisaSuccessSubscription = TaxEnrolmentSubscription(
    created = zonedDateTime,
    lastModified = zonedDateTime,
    credId = "",
    serviceName = "HMRC-LISA-ORG",
    identifiers = List(TaxEnrolmentIdentifier("ZREF", "Z1234")),
    callback = "",
    state = TaxEnrolmentSuccess,
    etmpId = "",
    groupIdentifier = ""
  )

  private val subs = List(lisaSuccessSubscription)

  "Get Subscriptions by Group ID endpoint" must {
    "return whatever it receives" in {
      stubForGet(subscriptionsUrl, OK, Json.toJson(subs).toString)

      val response: Seq[TaxEnrolmentSubscription] =
        await(taxEnrolmentConnector.getSubscriptionsByGroupId("1234567890"))

      response mustBe subs
    }
  }

}
