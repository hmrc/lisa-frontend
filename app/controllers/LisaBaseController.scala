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

package controllers

import config.FrontendAppConfig
import connectors.UserDetailsConnector
import models._
import play.api.Logger
import play.api.mvc.{AnyContent, Request, Result}
import services.TaxEnrolmentService
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait LisaBaseController extends FrontendController
  with AuthorisedFunctions
  with Redirects {

  val cache:ShortLivedCache
  val userDetailsConnector:UserDetailsConnector
  val taxEnrolmentService:TaxEnrolmentService

  def authorisedForLisa(callback: (String) => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    userStatus flatMap {
      case UserNotLoggedIn => Future.successful(toGGLogin(FrontendAppConfig.loginCallback))
      case UserUnauthorised => Future.successful(Redirect(routes.ErrorController.accessDenied()))
      case user: UserAuthorised => {
        getEnrolmentState(user.userDetails.groupIdentifier) flatMap {
          case TaxEnrolmentPending => Future.successful(Redirect(routes.ApplicationSubmittedController.pending()))
          case TaxEnrolmentError => Future.successful(Redirect(routes.ApplicationSubmittedController.rejected()))
          case TaxEnrolmentSuccess => Future.successful(Redirect(routes.ApplicationSubmittedController.successful()))
          case TaxEnrolmentDoesNotExist => callback(s"${user.internalId}-lisa-registration")
        }
      }
    } recover {
      case NonFatal(ex: Throwable) => {
        Logger.warn(s"Auth error: ${ex.getMessage}")
        Redirect(routes.ErrorController.error())
      }
    }
  }

  /* WIP - started refactoring auth & enrolment functionality with the aim of decoupling it from the base controller
   * to make things more modular and to make testing & mocking a whole lot simpler */

  trait LisaUserStatus
  case object UserNotLoggedIn extends LisaUserStatus
  case object UserUnauthorised extends LisaUserStatus
  case class UserAuthorised(internalId: String, userDetails: UserDetails) extends LisaUserStatus

  def userStatus(implicit hc:HeaderCarrier): Future[LisaUserStatus] = {
    authorised(
      AffinityGroup.Organisation and AuthProviders(GovernmentGateway)
    ).retrieve(internalId and userDetailsUri) { case (id ~ userUri) =>
      val userId = id.getOrElse(throw new RuntimeException("No internalId for logged in user"))

      getUserDetails(userUri)(hc) map { user =>
        UserAuthorised(userId, user)
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

  /* ------------------------- regular service below ---------------------------- */

  def hasAllSubmissionData(cacheId: String)(callback: (LisaRegistration) => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    // get organisation details
    cache.fetchAndGetEntry[OrganisationDetails](cacheId, OrganisationDetails.cacheKey).flatMap {
      case None => Future.successful(Redirect(routes.OrganisationDetailsController.get()))
      case Some(orgData) => {

        // get trading details
        cache.fetchAndGetEntry[TradingDetails](cacheId, TradingDetails.cacheKey).flatMap {
          case None => Future.successful(Redirect(routes.TradingDetailsController.get()))
          case Some(tradData) => {

            // get business structure
            cache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
              case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
              case Some(busData) => {

                // get user details
                cache.fetchAndGetEntry[YourDetails](cacheId, YourDetails.cacheKey).flatMap {
                  case None => Future.successful(Redirect(routes.YourDetailsController.get()))
                  case Some(yourData) => {
                    val data = new LisaRegistration(orgData, tradData, busData, yourData)
                    callback(data)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  def handleRedirect(redirectUrl: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val returnUrl: Option[String] = request.getQueryString("returnUrl")

    returnUrl match {
      case Some(url) if url.matches("^\\/lifetime\\-isa\\/.*$") => Future.successful(Redirect(url))
      case _ => Future.successful(Redirect(redirectUrl))
    }
  }

}
