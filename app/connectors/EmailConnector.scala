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
import metrics.EmailMetrics
import models.SendEmailRequest
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


sealed trait EmailStatus
case object EmailSent extends EmailStatus
case object EmailNotSent extends EmailStatus

class EmailConnector @Inject()(
  val http: WSHttp,
  val runModeConfiguration: Configuration,
  environment: Environment,
  metrics: EmailMetrics
) extends ServicesConfig with RawResponseReads {

  val mode = environment.mode
  val sendEmailUri: String = "hmrc/email"

  lazy val serviceUrl: String = baseUrl("email")

  def sendTemplatedEmail(emailAddress: String, templateName: String, params: Map[String, String])(implicit hc: HeaderCarrier): Future[EmailStatus] = {

    val sendEmailReq = SendEmailRequest(List(emailAddress), templateName, params, force = true)


    val postUrl = s"$serviceUrl/$sendEmailUri"
    val jsonData = Json.toJson(sendEmailReq)

    http.POST(postUrl, jsonData).map { response =>
      response.status match {
        case ACCEPTED => {
          Logger.info("Email sent successfully.")
          metrics.emailSentCounter()
          EmailSent
        }
        case status => {
          Logger.warn("Email not sent.")
          metrics.emailNotSentCounter()
          EmailNotSent
        }
      }
    }
  }
}