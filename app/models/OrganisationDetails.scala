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

package models

import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, OFormat}

case class OrganisationDetails(companyName: String, ctrNumber: String)

object OrganisationDetails {

  val cacheKey: String = "organisationDetails"
  val COMPANY_NAME_REGEX = """^[A-Za-z0-9 \-,.&'\/]{1,65}$"""
  val UTR_REGEX = "^[0-9]{10}$"

  implicit val formats: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]

  val form: Form[OrganisationDetails] = Form(
    mapping(
      "companyName" -> optional(text)
        .verifying("error.companyNameRequired", _.exists(_.trim.nonEmpty))
        .verifying("error.companyNameLength", i => i.isEmpty || i.getOrElse("").length <= 65)
        .verifying("error.companyNamePattern", i => i.isEmpty || i.getOrElse("").length > 65 || i.getOrElse("").matches(COMPANY_NAME_REGEX)),
      "ctrNumber" -> optional(text)
        .verifying("error.ctrNumberRequired", _.getOrElse("").matches("^.+$"))
        .verifying("error.ctrNumberLength", i => i.isEmpty || i.getOrElse("").length == 10)
        .verifying("error.ctrNumberPattern", i => i.isEmpty || i.getOrElse("").length != 10 || i.getOrElse("").matches(UTR_REGEX))
    )(
      (name, utr) => OrganisationDetails(
        name.getOrElse(""),
        utr.getOrElse("")
      )
    )(
      org => Some((Some(org.companyName), Some(org.ctrNumber)))
    )
  )

  val partnershipForm: Form[OrganisationDetails] = Form(
    mapping(
      "companyName" -> optional(text)
        .verifying("error.companyNameRequired", _.exists(_.trim.nonEmpty))
        .verifying("error.companyNameLength", i => i.isEmpty || i.getOrElse("").length <= 65)
        .verifying("error.companyNamePattern", i => i.isEmpty || i.getOrElse("").length > 65 || i.getOrElse("").matches(COMPANY_NAME_REGEX)),
      "strNumber" -> optional(text)
        .verifying("error.partnershipUtrRequired", _.getOrElse("").matches("^.+$"))
        .verifying("error.partnershipUtrLength", i => i.isEmpty || i.getOrElse("").length == 10)
        .verifying("error.partnershipUtrPattern", i => i.isEmpty || i.getOrElse("").length != 10 || i.getOrElse("").matches(UTR_REGEX))
    )(
      (name, utr) => OrganisationDetails(
        name.getOrElse(""),
        utr.getOrElse("")
      )
    )(
      org => Some((Some(org.companyName), Some(org.ctrNumber)))
    )
  )
}
