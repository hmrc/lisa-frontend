/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

class RosmConnector @Inject()(
  val httpClientV2: HttpClientV2,
  val appConfig: AppConfig
) (implicit ec: ExecutionContext) extends RosmJsonFormats with RawResponseReads {

  def registerOnce(utr: String, request:RosmRegistration)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val uri = s"${appConfig.lisaServiceUrl}/lisa/$utr/register"
    httpClientV2.post(url"$uri").withBody(Json.toJson(request)).execute[HttpResponse]
  }

  def subscribe(lisaManagerRef: String, lisaSubscribe:LisaSubscription)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val uri = s"${appConfig.lisaServiceUrl}/lisa/${lisaSubscribe.utr}/subscribe/$lisaManagerRef"
    httpClientV2.post(url"$uri").withBody(Json.toJson(lisaSubscribe)).execute[HttpResponse]
  }

}
