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

package services

import config.FrontendAuthConnector
import connectors.UserDetailsConnector
import models._
import play.api.Logger
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.Retrievals._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

trait AuthorisationService extends AuthorisedFunctions {

  val userDetailsConnector: UserDetailsConnector
  val taxEnrolmentService: TaxEnrolmentService

  def enrolmentAuthorised(implicit hc: HeaderCarrier): Future[Either[Boolean, String]] = {
    authorised(Enrolment("HMRC-LISA-ORG")).retrieve(authorisedEnrolments) { enr =>
      enr.getEnrolment("HMRC-LISA-ORG") match {
        case None => {
          Logger.warn("Authorised but no enrolment object")
          Future.successful(Left(false))
        }
        case Some(e) => {
          Logger.info("Got enrolment object for HMRC-LISA-ORG")
          Future.successful(Right(e.getIdentifier("ZREF").get.value))
        }
      }
    } recoverWith {
      case _ => {
        Logger.warn("The enrolment is not authorised")
        Future.successful(Left(false))
      }
    }
  }

  def userStatus(implicit hc: HeaderCarrier): Future[LisaUserStatus] = {
    authorised(
      AffinityGroup.Organisation and AuthProviders(GovernmentGateway)
    ).retrieve(internalId and userDetailsUri) { case (id ~ uri) =>
      val userId = id.getOrElse(throw new RuntimeException("No internalId for user"))
      val userUri = uri.getOrElse(throw new RuntimeException("No userDetailsUri for user"))

      userDetailsConnector.getUserDetails(userUri)(hc) flatMap { user =>
        val groupId = user.groupIdentifier.getOrElse(throw new RuntimeException("No groupIdentifier for user"))

        enrolmentAuthorised(hc) flatMap { res =>
          res match {
            case Right(zref) => {
              Logger.info("HMRC-LISA-ORG Enrolment is Authorised")
              Future.successful(UserAuthorisedAndEnrolled(userId, user, zref))
            }
            case Left(_) => {

              Logger.info("The enrolment has not been authorised yet so checking Enrolments service the group id is " + groupId)
              taxEnrolmentService.getNewestLisaSubscription(groupId)(hc) map {

                case Some(s) => {
                  Logger.info("The enrolments service came back with " + s.state.toString)
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
          }
        }
      }
    } recover {
      case _: NoActiveSession => UserNotLoggedIn
      case _: AuthorisationException => UserUnauthorised
    }
  }

}

object AuthorisationService extends AuthorisationService {
  val authConnector: AuthConnector = FrontendAuthConnector
  override val userDetailsConnector: UserDetailsConnector = UserDetailsConnector
  override val taxEnrolmentService: TaxEnrolmentService = TaxEnrolmentService
}