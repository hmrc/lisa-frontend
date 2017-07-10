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

package services

import config.FrontendAuthConnector
import connectors.UserDetailsConnector
import models._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AuthorisationService extends AuthorisedFunctions {

  val userDetailsConnector:UserDetailsConnector
  val taxEnrolmentService:TaxEnrolmentService

  def userStatus(implicit hc:HeaderCarrier): Future[LisaUserStatus] = {
    authorised(
      AffinityGroup.Organisation and AuthProviders(GovernmentGateway)
    ).retrieve(internalId and userDetailsUri) {case (id ~ uri) =>
      val userId = id.getOrElse(throw new RuntimeException("No internalId for user"))
      val userUri = uri.getOrElse(throw new RuntimeException("No userDetailsUri for user"))

      userDetailsConnector.getUserDetails(userUri)(hc) flatMap { user =>
        val groupId = user.groupIdentifier.getOrElse(throw new RuntimeException("No groupIdentifier for user"))

        taxEnrolmentService.getNewestLisaSubscription(groupId)(hc) map {
          case Some(s) => {
            s.state match {
              case TaxEnrolmentSuccess => {
                val zref = s.zref.getOrElse(throw new RuntimeException("No zref for successful enrolment"))

                UserAuthorisedAndEnrolled(userId, user, zref)
              }
              case _ => {
                UserAuthorised(userId, user, s.state)
              }
            }
          }
          case None => UserAuthorised(userId, user, TaxEnrolmentDoesNotExist)
        }
      }
    } recover {
      case _ : NoActiveSession => UserNotLoggedIn
      case _ : AuthorisationException => UserUnauthorised
    }
  }

}

object AuthorisationService extends AuthorisationService {
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  override val taxEnrolmentService: TaxEnrolmentService = TaxEnrolmentService
}