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

import models._

import java.time.{Instant, ZonedDateTime, ZoneId}
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait TaxEnrolmentJsonFormats {

  implicit val zonedDateTimeReads: Reads[ZonedDateTime] = Reads[ZonedDateTime] { json =>
    json.validate[Long].flatMap { timestamp =>
      try {
        val instant = Instant.ofEpochMilli(timestamp)
        val zonedDateTime: ZonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
        JsSuccess(zonedDateTime)
      } catch {
        case _: Throwable =>
          JsError(s"Invalid ZonedDateTime format for timestamp: $timestamp")
      }
    }
  }

  implicit val taxIdentifierFormats: OFormat[TaxEnrolmentIdentifier] = Json.format[TaxEnrolmentIdentifier]

  implicit val subscriptionReads: Reads[TaxEnrolmentSubscription] = (
    (JsPath \ "created").read[ZonedDateTime] and
    (JsPath \ "lastModified").read[ZonedDateTime] and
    (JsPath \ "credId").read[String] and
    (JsPath \ "serviceName").read[String] and
    (JsPath \ "identifiers").read[List[TaxEnrolmentIdentifier]].orElse(Reads.pure(Nil)) and
    (JsPath \ "callback").read[String] and
    (JsPath \ "state").read[String](Reads.pattern("^(ERROR|SUCCEEDED|PENDING)$".r, "error.formatting.state")).map[TaxEnrolmentState] {
      case "ERROR" => TaxEnrolmentError
      case "SUCCEEDED" => TaxEnrolmentSuccess
      case "PENDING" => TaxEnrolmentPending
    } and
    (JsPath \ "etmpId").read[String] and
    (JsPath \ "groupIdentifier").read[String]
  )(TaxEnrolmentSubscription.apply _)

  implicit val subscriptionWrites: Writes[TaxEnrolmentSubscription] = (
    (JsPath \ "created").write[Long].contramap[ZonedDateTime]{_.toInstant.toEpochMilli()} and
    (JsPath \ "lastModified").write[Long].contramap[ZonedDateTime]{_.toInstant.toEpochMilli()} and
    (JsPath \ "credId").write[String] and
    (JsPath \ "serviceName").write[String] and
    (JsPath \ "identifiers").write[List[TaxEnrolmentIdentifier]] and
    (JsPath \ "callback").write[String] and
    (JsPath \ "state").write[String].contramap[TaxEnrolmentState] {
      case TaxEnrolmentError => "ERROR"
      case TaxEnrolmentSuccess => "SUCCEEDED"
      case TaxEnrolmentPending => "PENDING"
    } and
    (JsPath \ "etmpId").write[String] and
    (JsPath \ "groupIdentifier").write[String]
  )(unlift(TaxEnrolmentSubscription.unapply))

}
