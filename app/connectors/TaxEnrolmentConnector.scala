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

package connectors

import config.WSHttp
import models._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

trait TaxEnrolmentConnector extends ServicesConfig with TaxEnrolmentJsonFormats {

  val httpGet: HttpGet = WSHttp
  val httpPut: HttpPut = WSHttp

  lazy val taxEnrolmentUrl: String = baseUrl("tax-enrolment")

  def addSubscriber(subscriptionId: String, safeId: String)(implicit hc: HeaderCarrier): Future[TaxEnrolmentAddSubscriber] = {
    val uri = s"$taxEnrolmentUrl/tax-enrolments/subscriptions/$subscriptionId/subscriber"

    val req = Json.obj(
      "serviceName" -> "HMRC-ORG-LISA",
      "callback" -> "",
      "etmpId" -> safeId
    )

    httpPut.PUT[JsValue, HttpResponse](uri, req)(implicitly, implicitly, hc).map(r =>
      r.status match {
        case 204 => TaxEnrolmentAddSubscriberSuccess
        case 400 => TaxEnrolmentAddSubscriberInvalid
        case 401 => TaxEnrolmentAddSubscriberUnauthorised
      }
    )
  }

  def getSubscriptionsByGroupId(groupId: String)(implicit hc: HeaderCarrier): Future[List[TaxEnrolmentSubscription]] = {
    val uri = s"$taxEnrolmentUrl/tax-enrolments/groups/$groupId/subscriptions"

    httpGet.GET[List[TaxEnrolmentSubscription]](uri)(implicitly, hc)
  }

}

