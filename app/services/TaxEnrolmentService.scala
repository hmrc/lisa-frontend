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

package services

import com.google.inject.Inject
import connectors.TaxEnrolmentConnector
import models._
import java.time.ZonedDateTime

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

class TaxEnrolmentService @Inject()(val connector: TaxEnrolmentConnector) (implicit ec: ExecutionContext) {

  implicit def dateTimeOrdering: Ordering[ZonedDateTime] = Ordering.fromLessThan(_ isBefore _)

  def getNewestLisaSubscription(groupId: String)(implicit hc:HeaderCarrier): Future[Option[TaxEnrolmentSubscription]] = {
    val response: Future[List[TaxEnrolmentSubscription]] = connector.getSubscriptionsByGroupId(groupId)(hc)

    response.map { l =>
      val subs = l.filter(sub => sub.serviceName == "HMRC-LISA-ORG")
      if (subs.isEmpty) {
        None
      }
      else {
        Some(subs.maxBy(sub => sub.created))
      }
    }
  }

}
