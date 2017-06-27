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
    (JsPath \ "created").read[String].map[DateTime](d => new DateTime(d)) and
    (JsPath \ "lastModified").read[String].map[DateTime](d => new DateTime(d)) and
    (JsPath \ "credId").read[String] and
    (JsPath \ "serviceName").read[String] and
    (JsPath \ "identifiers").read[List[TaxEnrolmentIdentifier]] and
    (JsPath \ "callback").read[String] and
    (JsPath \ "state").read[String](Reads.pattern("^(ERROR|SUCCESS|PENDING)$".r, "error.formatting.state")).map[TaxEnrolmentState] {
      case "ERROR" => TaxEnrolmentError
      case "SUCCESS" => TaxEnrolmentSuccess
      case "PENDING" => TaxEnrolmentPending
    } and
    (JsPath \ "etmpId").read[String] and
    (JsPath \ "groupIdentifier").read[String]
  )(TaxEnrolmentSubscription.apply _)

}