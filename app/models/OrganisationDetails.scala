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

package models

import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}

case class OrganisationDetails(companyName: String, ctrNumber: String, safeId:Option[String])

object OrganisationDetails {

  val cacheKey: String = "organisationDetails"

  implicit val formats: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]

  val form: Form[OrganisationDetails] = Form(
    mapping(
      compLabel-> text.verifying(nonEmptyTextLisa(company_error_key),companyPattern),
      utrLabel -> text.verifying(nonEmptyTextLisa(ctutr_error_key)))((companyName,ctrNumber) =>
      OrganisationDetails(companyName,ctrNumber,Some("")))
    (organisationDetails => Some((organisationDetails.companyName,organisationDetails.ctrNumber)))
  )
}