/*
 * Copyright 2021 HM Revenue & Customs
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
import metrics.EmailMetrics
import models.SendEmailRequest
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


sealed trait EmailStatus
case object EmailSent extends EmailStatus
case object EmailNotSent extends EmailStatus

class EmailConnector @Inject()(
  val http: HttpClient,
  appConfig: AppConfig,
  metrics: EmailMetrics
) extends RawResponseReads with Logging {

  def sendTemplatedEmail(emailAddress: String, templateName: String, params: Map[String, String])(implicit hc: HeaderCarrier): Future[EmailStatus] = {

    val sendEmailReq = SendEmailRequest(List(emailAddress), templateName, params, force = true)

    val postUrl = s"${appConfig.emailServiceUrl}/hmrc/email"
    val jsonData = Json.toJson(sendEmailReq)

    http.POST(postUrl, jsonData).map { response =>
      response.status match {
        case ACCEPTED => {
          logger.info("Email sent successfully.")
          metrics.emailSentCounter()
          EmailSent
        }
        case status => {
          logger.warn("Email not sent.")
          metrics.emailNotSentCounter()
          EmailNotSent
        }
      }
    }
  }
}