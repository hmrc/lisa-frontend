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
      "firstName" -> text.verifying(pattern("""^[A-Za-z0-9 \-,.&'\\]{1,35}$""".r, error="error.firstName")),
      "lastName" -> text.verifying(pattern("""^[A-Za-z0-9 \-,.&'\\]{1,35}$""".r, error="error.lastName")),
      "role" -> text.verifying(pattern("""^[A-Za-z0-9 \-,.&'\/]{1,30}$""".r, error="error.role")),
      "phone" -> text.verifying(pattern("""^[A-Z0-9 \)\/\(\*\#\-\+]{1,24}$""".r, error="error.phone")),
      "email" -> email
    )(YourDetails.apply)(YourDetails.unapply)
  )
}