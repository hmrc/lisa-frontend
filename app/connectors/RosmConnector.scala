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
import models.{RosmRegistration, RosmRegistrationFailureResponse, RosmRegistrationResponse, RosmRegistrationSuccessResponse}
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RosmConnector extends ServicesConfig with RosmJsonFormats {

  val httpPost:HttpPost = WSHttp
  lazy val rosmUrl = baseUrl("rosm")

  val httpReads:HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse) = response
  }

  def registerOnce(utr: String, request:RosmRegistration)(implicit hc: HeaderCarrier): Future[RosmRegistrationResponse] = {
    val uri = s"$rosmUrl/registration/organisation/utr/$utr"
    val result = httpPost.POST[RosmRegistration, HttpResponse](uri, request)(implicitly, httpReads, implicitly)

    result map (r =>
      r.json.validate[RosmRegistrationSuccessResponse] match {
        case success: JsSuccess[RosmRegistrationSuccessResponse] => success.get
        case error: JsError => parseError(r.json)
      }
    )
  }

  private def parseError(json:JsValue):RosmRegistrationFailureResponse = {
    json.validate[RosmRegistrationFailureResponse] match {
      case success: JsSuccess[RosmRegistrationFailureResponse] => success.get
      case failure: JsError => RosmRegistrationFailureResponse(
        code = "INTERNAL_SERVER_ERROR",
        reason = "Internal Server Error")
    }
  }

}

object RosmConnector extends RosmConnector {

}