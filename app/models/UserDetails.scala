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

trait LisaUserStatus
case object UserNotLoggedIn extends LisaUserStatus
case object UserUnauthorised extends LisaUserStatus
case class UserAuthorised(internalId: String, userDetails: UserDetails, enrolmentState: TaxEnrolmentState) extends LisaUserStatus
case class UserAuthorisedAndEnrolled(internalId: String, userDetails: UserDetails, lisaManagerReferenceNumber: String) extends LisaUserStatus

case class UserDetails(authProviderId: Option[String],
                       authProviderType: Option[String],
                       name: String,
                       lastName: Option[String] = None,
                       dateOfBirth: Option[String] = None,
                       postCode: Option[String] = None,
                       email: Option[String] = None,
                       affinityGroup: Option[String] = None,
                       agentCode: Option[String] = None,
                       agentId: Option[String] = None,
                       agentFriendlyName: Option[String] = None,
                       credentialRole: Option[String] = None,
                       description: Option[String] = None,
                       groupIdentifier: Option[String] = None)