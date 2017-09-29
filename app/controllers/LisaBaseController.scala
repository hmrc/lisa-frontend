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
import models._
import org.apache.commons.io.filefilter.FalseFileFilter
import play.api.Logger
import play.api.mvc.{AnyContent, Request, Result}
import services.AuthorisationService
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait LisaBaseController extends FrontendController
  with Redirects {

  val sessionCache: SessionCache
  val shortLivedCache: ShortLivedCache
  val authorisationService: AuthorisationService

  def authorisedForLisa(callback: (String) => Future[Result], checkEnrolmentState: Boolean = true)
                       (implicit request: Request[AnyContent]): Future[Result] = {
    authorisationService.userStatus flatMap {
      case UserNotLoggedIn => Future.successful(toGGLogin(FrontendAppConfig.loginCallback))
      case UserUnauthorised => Future.successful(Redirect(routes.ErrorController.accessDenied()))
      case user: UserAuthorisedAndEnrolled => handleUserAuthorisedAndEnrolled(callback, checkEnrolmentState, user)
      case user: UserAuthorised =>  getCheckEnrolmentState(user, checkEnrolmentState) flatMap { bool =>
        handleUserAuthorised(callback, bool, user)
      }
    }
  }

  private def getCheckEnrolmentState(user: UserAuthorised, checkEnrolmentState: Boolean)(implicit request: Request[AnyContent]): Future[Boolean] = {
    shortLivedCache.fetchAndGetEntry[Boolean](s"${user.internalId}-lisa-registration", Reapplication.cachKey) map { bool =>
      bool
      match {
          case Some(true) => false
          case _ => checkEnrolmentState
      }
    }
  }

  private def handleUserAuthorised(callback: (String) => Future[Result], checkEnrolmentState: Boolean, user: UserAuthorised)
                                  (implicit request: Request[AnyContent]): Future[Result] = {
    Logger.warn("User Authorised")

    if (checkEnrolmentState) {
      user.enrolmentState match {
        case TaxEnrolmentPending => {
          Logger.warn("Enrollment Pending")
          Future.successful(Redirect(routes.ApplicationSubmittedController.pending()))
        }
        case TaxEnrolmentError => {
          Logger.warn("Enrollment Rejected")
          Future.successful(Redirect(routes.ApplicationSubmittedController.rejected()))
        }
        case TaxEnrolmentDoesNotExist => {
          Logger.warn("Enrollment Does Not Exist")
          callback(s"${user.internalId}-lisa-registration")
        }
      }
    }
    else {
      callback(s"${user.internalId}-lisa-registration")
    }
  }

  private def handleUserAuthorisedAndEnrolled(callback: (String) => Future[Result], checkEnrolmentState: Boolean, user: UserAuthorisedAndEnrolled)
                                            (implicit request: Request[AnyContent]): Future[Result] = {
    Logger.warn("User Authorised And Enrolled")

    if (checkEnrolmentState) {
      sessionCache.cache[String]("lisaManagerReferenceNumber", user.lisaManagerReferenceNumber)

      Future.successful(Redirect(routes.ApplicationSubmittedController.successful()))
    }
    else {
      callback(s"${user.internalId}-lisa-registration")
    }
  }

  def hasAllSubmissionData(cacheId: String)(callback: (LisaRegistration) => Future[Result])
                          (implicit request: Request[AnyContent]): Future[Result] = {

    // get business structure
    shortLivedCache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
      case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
      case Some(busData) => {

        // get organisation details
        shortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, OrganisationDetails.cacheKey).flatMap {
          case None => Future.successful(Redirect(routes.OrganisationDetailsController.get()))
          case Some(orgData) => {

            // get safe Id
            shortLivedCache.fetchAndGetEntry[String](cacheId, "safeId").flatMap {
              case None => Future.successful(Redirect(routes.OrganisationDetailsController.get()))
              case Some(safeId) => {

                // get trading details
                shortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, TradingDetails.cacheKey).flatMap {
                  case None => Future.successful(Redirect(routes.TradingDetailsController.get()))
                  case Some(tradData) => {

                    // get user details
                    shortLivedCache.fetchAndGetEntry[YourDetails](cacheId, YourDetails.cacheKey).flatMap {
                      case None => Future.successful(Redirect(routes.YourDetailsController.get()))
                      case Some(yourData) => {
                        val data = new LisaRegistration(orgData, tradData, busData, yourData, safeId)
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
