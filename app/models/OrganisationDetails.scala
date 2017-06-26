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
import play.api.data.validation.Constraints.pattern
import play.api.data.validation.{Constraint, Valid, Invalid, ValidationError}
import play.api.libs.json.{Json, OFormat}

case class OrganisationDetails(companyName: String, ctrNumber: String, safeId:Option[String])

object OrganisationDetails {

  def nonEmptyTextLisa[T](messageKey:String): Constraint[String] = Constraint[String]("constraint.required") { text =>
    if (text == null) Invalid(messageKey) else if (text.trim.isEmpty) Invalid(ValidationError(messageKey)) else Valid
  }

  val pat = pattern("""^[a-zA-Z0-9 '&\\/]{0,105}$""".r, error="Invalid company name")

  implicit val formats: OFormat[OrganisationDetails] = Json.format[OrganisationDetails]
  val cacheKey: String = "organisationDetails"
  val form: Form[OrganisationDetails] = Form(
    mapping(
      "companyName" -> text.verifying(nonEmptyTextLisa("org.compName.mandatory"),pattern("""^[a-zA-Z0-9 '&\\/]{0,105}$""".r, error="Invalid company name")),
      "ctrNumber" -> text.verifying(nonEmptyTextLisa("org.ctUtr.mandatory")))((companyName,ctrNumber) => OrganisationDetails(companyName,ctrNumber,Some("")))
    (organisationDetails => Some((organisationDetails.companyName,organisationDetails.ctrNumber)))
  )
}