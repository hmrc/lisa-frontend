/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TaxEnrolmentConnector @Inject()(
  val httpGet: HttpClient,
  val appConfig: AppConfig
) extends TaxEnrolmentJsonFormats {

  def getSubscriptionsByGroupId(groupId: String)(implicit hc: HeaderCarrier): Future[List[TaxEnrolmentSubscription]] = {
    val uri = s"${appConfig.lisaServiceUrl}/lisa/tax-enrolments/groups/$groupId/subscriptions"

    httpGet.GET[HttpResponse](uri)(implicitly, hc, implicitly) map { res =>
      Logger.debug(s"Getsubscriptions returned status: ${res.status} ")
      Json.parse(res.body).as[List[TaxEnrolmentSubscription]]
    }
  }
}