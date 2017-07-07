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

import models._
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait TaxEnrolmentJsonFormats {

  implicit val taxIdentifierFormats: OFormat[TaxEnrolmentIdentifier] = Json.format[TaxEnrolmentIdentifier]

  implicit val subscriptionReads: Reads[TaxEnrolmentSubscription] = (
    (JsPath \ "created").read[Long].map[DateTime](d => new DateTime(d)) and
    (JsPath \ "lastModified").read[Long].map[DateTime](d => new DateTime(d)) and
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
  )( (created, lastModified, credId, serviceName, identifiers, callback, state, etmpId, groupIdentifier) => {
    val zrefs = identifiers.filter(id => id.key == "ZREF").map(id => id.value)

    TaxEnrolmentSubscription(
      created = created,
      lastModified = lastModified,
      credId = credId,
      serviceName = serviceName,
      identifiers = identifiers,
      callback = callback,
      state = if (state == TaxEnrolmentSuccess && !zrefs.isEmpty) TaxEnrolmentSuccess(zrefs.head) else state,
      etmpId = etmpId,
      groupIdentifier = groupIdentifier
    )
  })

  implicit val subscriptionWrites: Writes[TaxEnrolmentSubscription] = (
    (JsPath \ "created").write[Long].contramap[DateTime]{_.getMillis} and
    (JsPath \ "lastModified").write[Long].contramap[DateTime]{_.getMillis} and
    (JsPath \ "credId").write[String] and
    (JsPath \ "serviceName").write[String] and
    (JsPath \ "identifiers").write[List[TaxEnrolmentIdentifier]] and
    (JsPath \ "callback").write[String] and
    (JsPath \ "state").write[String].contramap[TaxEnrolmentState] {
      case TaxEnrolmentError => "ERROR"
      case TaxEnrolmentSuccess => "SUCCEEDED"
      case TaxEnrolmentSuccess(_) => "SUCCEEDED"
      case TaxEnrolmentPending => "PENDING"
    } and
    (JsPath \ "etmpId").write[String] and
    (JsPath \ "groupIdentifier").write[String]
  ) (unlift(TaxEnrolmentSubscription.unapply))

}