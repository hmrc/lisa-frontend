/*
 * Copyright 2019 HM Revenue & Customs
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

case class YourDetails(firstName: String,
                       lastName: String,
                       role: String,
                       phone: String,
                       email: String)

object YourDetails {
  implicit val formats: OFormat[YourDetails] = Json.format[YourDetails]

  val cacheKey = "yourDetails"

  val form = Form(
    mapping(
      "firstName" -> optional(text)
        .verifying("error.firstNameRequired", _.isDefined)
        .verifying("error.firstNameLength", i => i.isEmpty || i.getOrElse("").length <= 35)
        .verifying("error.firstNamePattern", i => i.isEmpty || i.getOrElse("").length > 35 || i.getOrElse("").matches("""^[A-Za-z0-9 \-,.&'\\]{1,35}$""")),

      "lastName" -> optional(text)
        .verifying("error.lastNameRequired", _.isDefined)
        .verifying("error.lastNameLength", i => i.isEmpty || i.getOrElse("").length <= 35)
        .verifying("error.lastNamePattern", i => i.isEmpty || i.getOrElse("").length > 35 || i.getOrElse("").matches("""^[A-Za-z0-9 \-,.&'\\]{1,35}$""")),

      "role" -> optional(text)
        .verifying("error.roleRequired", _.isDefined)
        .verifying("error.roleLength", i => i.isEmpty || i.getOrElse("").length <= 30)
        .verifying("error.rolePattern", i => i.isEmpty || i.getOrElse("").length > 30 || i.getOrElse("").matches("""^[A-Za-z0-9 \-,.&'\/]{1,30}$""")),

      "phone" -> optional(text)
        .verifying("error.phoneRequired", _.isDefined)
        .verifying("error.phonePattern", i => i.isEmpty || i.getOrElse("").matches("""^[A-Z0-9 \)\/\(\*\#\-\+]{1,24}$""")),

      "email" -> optional(email)
        .verifying("error.emailRequired", _.isDefined)
    )(
      (firstName, lastName, role, phone, email) => YourDetails(
        firstName.getOrElse(""),
        lastName.getOrElse(""),
        role.getOrElse(""),
        phone.getOrElse(""),
        email.getOrElse("")
      )
    )(
      details => Some(Some(details.firstName), Some(details.lastName), Some(details.role), Some(details.phone), Some(details.email))
    )
  )
}