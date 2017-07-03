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

import play.api.data.validation.Constraints._
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}


package object models extends Constants{

  val companyPattern: Constraint[String] = pattern("""^[A-Za-z0-9 \-,.&'\/]{1,65}$""".r, error="Enter a valid company name.")
  val utrPattern: Constraint[String] = pattern("""^[0-9]{10}$""".r, error="Enter a valid Tax Reference Number.")
  val fcaPattern: Constraint[String] = pattern("""^[0-9]{6}$""".r, error="Enter a valid Financial Number.")
  val isaPattern: Constraint[String] = pattern("""^Z([0-9]{4}|[0-9]{6})$""".r, error="Enter a valid ISA ref number. This" +
    " starts with Z, and includes either 4 or 6 numbers.")
  val rolePattern: Constraint[String] = pattern("""^[A-Za-z0-9 \-,.&'\/]{1,30}$""".r, error="Enter a valid role in " +
    "the organisation. You can enter up to 30 characters.")
  val phoneNumberPattern: Constraint[String] = pattern("""^[A-Z0-9 \)\/\(\*\#\-\+]{1,24}$""".r, error="Enter a valid contact phone number.")
  def namePattern(error: String): Constraint[String] = {
    pattern("""^[A-Za-z0-9 \-,.&'\\]{1,35}$""".r, error=error)

  }
  def nonEmptyTextLisa[T](messageKey:String): Constraint[String] = Constraint[String](required) { text =>
    if (text == null) Invalid(messageKey) else if (text.trim.isEmpty) Invalid(ValidationError(messageKey)) else Valid
  }
}

trait Constants {

  val company_error_key:String = "org.compName.mandatory"
  val ctutr_error_key:String =  "org.ctUtr.mandatory"
  val fca_error_key:String = "org.fca.manadtory"
  val isaprovider_error_key: String = "org.isa.mandatory"
  val compLabel:String = "companyName"
  val utrLabel:String = "ctrNumber"
  val required :String= "constraint.required"
}
