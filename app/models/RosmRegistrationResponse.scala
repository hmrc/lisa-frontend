/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate

trait RosmRegistrationResponse

case class RosmRegistrationSuccessResponse(safeId: String,
                                           agentReferenceNumber: Option[String] = None,
                                           isEditable: Boolean,
                                           isAnAgent: Boolean,
                                           isAnASAgent: Option[Boolean] = None,
                                           isAnIndividual: Boolean,
                                           individual: Option[RosmIndividual] = None,
                                           organisation: Option[RosmOrganisation] = None,
                                           address: RosmAddress,
                                           contactDetails: RosmContactDetails) extends RosmRegistrationResponse

case class DesFailureResponse(code: String, reason: String)

case class RosmIndividual(firstName: String,
                          middleName: Option[String] = None,
                          lastName: String,
                          dateOfBirth: Option[LocalDate] = None)

case class RosmOrganisation(organisationName: String,
                            isAGroup: Option[Boolean] = None,
                            organisationType: Option[String] = None)

case class RosmAddress(addressLine1: String,
                       addressLine2: Option[String] = None,
                       addressLine3: Option[String] = None,
                       addressLine4: Option[String] = None,
                       countryCode: String,
                       postalCode: Option[String] = None)

case class RosmContactDetails(primaryPhoneNumber: Option[String] = None,
                              secondaryPhoneNumber: Option[String] = None,
                              faxNumber: Option[String] = None,
                              emailAddress: Option[String] = None)


case class DesSubscriptionSuccessResponse(subscriptionId:String)
