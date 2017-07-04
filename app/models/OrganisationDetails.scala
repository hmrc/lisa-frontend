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
      "companyName" -> text.verifying(pattern("""^[A-Za-z0-9 \-,.&'\/]{1,65}$""".r, error="Enter a valid company name.")),
      "ctrNumber" -> text.verifying(pattern("""^[0-9]{10}$""".r, error="Enter a unique tax reference number that is 10 characters long."))
    )(OrganisationDetails.apply)(OrganisationDetails.unapply)
  )
}