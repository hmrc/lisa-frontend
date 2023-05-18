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

package connectors

import com.google.inject.Inject
import config.AppConfig
import models._
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class TaxEnrolmentConnector @Inject()(
  val httpGet: HttpClient,
  val appConfig: AppConfig
) (implicit ec: ExecutionContext) extends TaxEnrolmentJsonFormats with Logging with RawResponseReads {

  def getSubscriptionsByGroupId(groupId: String)(implicit hc: HeaderCarrier): Future[List[TaxEnrolmentSubscription]] = {
    val uri = s"${appConfig.lisaServiceUrl}/lisa/tax-enrolments/groups/$groupId/subscriptions"

    httpGet.GET[HttpResponse](uri)(httpReads, hc, implicitly) map { res =>
      logger.debug(s"Getsubscriptions returned status: ${res.status} ")
      Json.parse(res.body).as[List[TaxEnrolmentSubscription]]
    }
  }
}
