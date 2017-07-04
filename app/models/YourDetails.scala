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

case class YourDetails(firstName: String,
                       lastName: String,
                       role: String,
                       phone: String,
                       email: String)

object YourDetails {
  implicit val formats: OFormat[YourDetails] = Json.format[YourDetails]

  val cacheKey = "yourDetails"

  val form: Form[YourDetails] = Form(

    mapping(
      firstNameLabel -> text.verifying(nonEmptyTextLisa(firstname_error_key),namePattern),
      lastNameLabel -> text.verifying(nonEmptyTextLisa(lastname_error_key),namePattern),
      roleLabel -> text.verifying(nonEmptyTextLisa(role_error_key),rolePattern),
      phoneLabel -> text.verifying(nonEmptyTextLisa(phone_error_key),phoneNumberPattern),
      "email" -> email)
   ((firstName,lastName, role, phone, email) =>
    YourDetails(firstName, lastName, role, phone, email))
  (yourdetails => Some((yourdetails.firstName, yourdetails.lastName, yourdetails.role,yourdetails.phone, yourdetails.email)))

  )
}