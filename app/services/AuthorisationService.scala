/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import com.google.inject.Inject
import models.*
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.*
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthorisationService @Inject() (val authConnector: AuthConnector, val taxEnrolmentService: TaxEnrolmentService)(
  using ec: ExecutionContext
) extends AuthorisedFunctions {

  def userStatus(using hc: HeaderCarrier): Future[UserStatus] =
    authorised(
      AffinityGroup.Organisation and AuthProviders(GovernmentGateway) and User
    ).retrieve(internalId and groupIdentifier and authorisedEnrolments) {
      case Some(id) ~ Some(groupId) ~ enrolments =>
        statusFromAuth(id, enrolments)
          .map(a => Future.successful(Some(a)))
          .getOrElse(statusFromTaxEnrolments(id, groupId))
          .map(_.getOrElse(UserAuthorised(id, TaxEnrolmentDoesNotExist)))
      case _                                     => Future.successful(UserUnauthorised)
    } recover {
      case _: UnsupportedCredentialRole => UserNotAdmin
      case _: NoActiveSession           => UserNotLoggedIn
      case _: AuthorisationException    => UserUnauthorised
    }

  def statusFromAuth(id: String, enrolments: Enrolments): Option[UserStatus] =
    for {
      enrolment <- enrolments.getEnrolment("HMRC-LISA-ORG")
      zref      <- enrolment.getIdentifier("ZREF") if enrolment.isActivated
    } yield UserAuthorisedAndEnrolled(id, zref.value)

  def statusFromTaxEnrolments(id: String, groupId: String)(using hc: HeaderCarrier): Future[Option[UserStatus]] =
    taxEnrolmentService.getNewestLisaSubscription(groupId)(using hc).map {
      _.flatMap { subscription =>
        subscription.state match {
          case TaxEnrolmentSuccess      =>
            subscription.zref.map(UserAuthorisedAndEnrolled(id, _))
          case state: TaxEnrolmentState =>
            Some(UserAuthorised(id, state))
        }
      }
    }

}
