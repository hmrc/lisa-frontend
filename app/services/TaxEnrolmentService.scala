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

package services

import connectors.TaxEnrolmentConnector
import models._
import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

trait TaxEnrolmentService {

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  val connector: TaxEnrolmentConnector

  def addSubscriber(subscriptionId: String, safeId: String)(implicit hc:HeaderCarrier): Future[TaxEnrolmentAddSubscriberResponse] = {
    val request = TaxEnrolmentAddSubscriberRequest("HMRC-ORG-LISA", "", safeId)
    val response = connector.addSubscriber(subscriptionId, request)(hc)

    response.map { _ =>
      Logger.info(s"Tax Enrolment Subscribe accepted for $subscriptionId.")
      TaxEnrolmentAddSubscriberSucceeded
    } recover {
      case NonFatal(ex:Exception) =>
        Logger.error(s"Tax Enrolment Subscribe failed for $subscriptionId. Exception : ${ex.getMessage}")
        TaxEnrolmentAddSubscriberFailed
    }
  }

  def getLisaSubscriptionStatus(groupId: String)(implicit hc:HeaderCarrier): Future[TaxEnrolmentState] = {
    val response: Future[List[TaxEnrolmentSubscription]] = connector.getSubscriptionsByGroupId(groupId)(hc)

    response.map { l =>
      l.filter(sub => sub.serviceName == "HMRC-LISA-ORG").maxBy(sub => sub.created).state
    }
  }

}

object TaxEnrolmentService extends TaxEnrolmentService {
  override val connector: TaxEnrolmentConnector = TaxEnrolmentConnector
}
