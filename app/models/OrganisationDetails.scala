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

package models

import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints.pattern
import play.api.libs.json.{Json, OFormat}

case class OrganisationDetails(companyName: String, ctrNumber: String)

object OrganisationDetails {

  val cacheKey: String = "organisationDetails"

  implicit val formats: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]

  val form = Form(
    mapping(
      "companyName" -> optional(text)
        .verifying("error.companyName", i => i.getOrElse("").matches("""^[A-Za-z0-9 \-,.&'\/]{1,65}$""")),
      "ctrNumber" -> optional(text)
        .verifying("error.ctrNumber", i => i.isDefined)
        .verifying("error.ctrNumberPattern", i => i.getOrElse("").matches("(^$)|(^[0-9]{10}$)"))
    )((name, utr) => OrganisationDetails(name.getOrElse(""), utr.getOrElse("")))( org => Some(Some(org.companyName), Some(org.ctrNumber)) )
  )

  val partnershipForm = Form(
    mapping(
      "companyName" -> optional(text)
        .verifying("error.companyName", i => i.getOrElse("").matches("""^[A-Za-z0-9 \-,.&'\/]{1,65}$""")),
      "ctrNumber" -> optional(text)
        .verifying("error.partnershipUtr", i => i.isDefined)
        .verifying("error.partnershipUtrPattern", i => i.getOrElse("").matches("(^$)|(^[0-9]{10}$)"))
    )((name, utr) => OrganisationDetails(name.getOrElse(""), utr.getOrElse("")))( org => Some(Some(org.companyName), Some(org.ctrNumber)) )
  )
}