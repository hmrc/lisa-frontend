/*
 * Copyright 2026 HM Revenue & Customs
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

import models.*

import java.time.LocalDate
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

trait RosmJsonFormats {

  given indReads: Reads[RosmIndividual] = (
    (JsPath \ "firstName").read[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "dateOfBirth").readNullable[String].map {
        case Some(date) => Some(LocalDate.parse(date))
        case _          => None
      }
  )(RosmIndividual.apply _)

  given indWrites: Writes[RosmIndividual] = (
    (JsPath \ "firstName").write[String] and
      (JsPath \ "middleName").writeNullable[String] and
      (JsPath \ "lastName").write[String] and
      (JsPath \ "dateOfBirth").writeNullable[String].contramap[Option[LocalDate]] {
        case Some(date) => Some(date.toString)
        case _          => None
      }
  )(o => Tuple.fromProductTyped(o))

  given orgFormats: OFormat[RosmOrganisation]                 = Json.format[RosmOrganisation]
  given addrFormats: OFormat[RosmAddress]                     = Json.format[RosmAddress]
  given contFormats: OFormat[RosmContactDetails]              = Json.format[RosmContactDetails]
  given succFormats: OFormat[RosmRegistrationSuccessResponse] = Json.format[RosmRegistrationSuccessResponse]
  given failFormats: OFormat[DesFailureResponse]              = Json.format[DesFailureResponse]

  given desSubscribeFormats: OFormat[DesSubscriptionSuccessResponse] =
    Json.format[DesSubscriptionSuccessResponse]

}
