/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpGet, HttpResponse }
import scala.concurrent.ExecutionContext.Implicits.global

trait TaxEnrolmentConnector extends ServicesConfig with TaxEnrolmentJsonFormats {

  val httpGet: HttpGet = WSHttp

  lazy val serviceUrl: String = baseUrl("lisa")

  def getSubscriptionsByGroupId(groupId: String)(implicit hc: HeaderCarrier): Future[List[TaxEnrolmentSubscription]] = {
    val uri = s"$serviceUrl/lisa/tax-enrolments/groups/$groupId/subscriptions"

    httpGet.GET[HttpResponse](uri)(implicitly, hc, MdcLoggingExecutionContext.fromLoggingDetails(hc)) map { res =>
      Logger.debug(s"Getsubscriptions returned status: ${res.status} ")
      Json.parse(res.body).as[List[TaxEnrolmentSubscription]]
    }
  }
}

object TaxEnrolmentConnector extends TaxEnrolmentConnector