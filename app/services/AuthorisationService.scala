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
import play.api.{Configuration, Environment, Play}
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
    ).retrieve(internalId and userDetailsUri) { case (id ~ userUri) =>
      val userId = id.getOrElse(throw new RuntimeException("No internalId for logged in user"))

      getUserDetails(userUri)(hc) flatMap { user =>
        getEnrolmentState(user.groupIdentifier)(hc) map { state =>
          UserAuthorised(userId, user, state)
        }
      }
    } recover {
      case _ : NoActiveSession => UserNotLoggedIn
      case _ : AuthorisationException => UserUnauthorised
    }
  }

  def getUserDetails(userDetailsUri: Option[String])(implicit hc:HeaderCarrier): Future[UserDetails] = {
    userDetailsUri match {
      case Some(url) => {
        userDetailsConnector.getUserDetails(url)(hc)
      }
      case None => {
        Future.failed(new RuntimeException("No userDetailsUri"))
      }
    }
  }

  def getEnrolmentState(groupIdentifier: Option[String])(implicit hc:HeaderCarrier): Future[TaxEnrolmentState] = {
    groupIdentifier match {
      case Some(groupId) => taxEnrolmentService.getLisaSubscriptionState(groupId)
      case None => Future.failed(new RuntimeException("Could not get groupIdentifier"))
    }
  }

}

object AuthorisationService extends AuthorisationService {
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  override val taxEnrolmentService: TaxEnrolmentService = TaxEnrolmentService
}