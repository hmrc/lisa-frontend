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

import play.api.libs.json.{Json, OFormat}

case class ContactDetails(phoneNumber:String,emailAddress:String)
object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

case class ApplicantDetails(name:String, surname:String, position:String, contactDetails:ContactDetails)
object ApplicantDetails {
  implicit val format: OFormat[ApplicantDetails] = Json.format[ApplicantDetails]
}

case class LisaSubscription (
                              utr:String,
                              safeId:String,
                              approvalNumber:String,
                              companyName: String,
                              applicantDetails: ApplicantDetails
                              )

object LisaSubscription {
  implicit val format: OFormat[LisaSubscription] = Json.format[LisaSubscription]
}
