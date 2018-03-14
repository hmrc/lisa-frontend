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

import models._
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait RosmJsonFormats {

  implicit val indReads: Reads[RosmIndividual] = (
    (JsPath \ "firstName").read[String] and
    (JsPath \ "middleName").readNullable[String] and
    (JsPath \ "lastName").read[String] and
    (JsPath \ "dateOfBirth").readNullable[String].map {
      case Some(date) => Some(new DateTime(date))
      case _ => None
    }
  )(RosmIndividual.apply _)

  implicit val indWrites: Writes[RosmIndividual] = (
    (JsPath \ "firstName").write[String] and
    (JsPath \ "middleName").writeNullable[String] and
    (JsPath \ "lastName").write[String] and
    (JsPath \ "dateOfBirth").writeNullable[String].contramap[Option[DateTime]] {
      case Some(date) => Some(date.toString("yyyy-MM-dd"))
      case _ => None
    }
  )(unlift(RosmIndividual.unapply))

  implicit val orgFormats = Json.format[RosmOrganisation]
  implicit val addrFormats = Json.format[RosmAddress]
  implicit val contFormats = Json.format[RosmContactDetails]
  implicit val succFormats = Json.format[RosmRegistrationSuccessResponse]
  implicit val failFormats = Json.format[DesFailureResponse]
  implicit val desSubscribeFormats = Json.format[DesSubscriptionSuccessResponse]


}