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
import play.api.Logger
import play.api.mvc.{AnyContent, Request, Result}
import services.AuthorisationService
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future
import scala.util.control.NonFatal

trait LisaBaseController extends FrontendController
  with Redirects {

  val cache:ShortLivedCache
  val authorisationService:AuthorisationService

  def authorisedForLisa(callback: (String) => Future[Result], checkEnrolmentStates: Boolean = true)(implicit request: Request[AnyContent]): Future[Result] = {
    authorisationService.userStatus flatMap {
      case UserNotLoggedIn => Future.successful(toGGLogin(FrontendAppConfig.loginCallback))
      case UserUnauthorised => Future.successful(Redirect(routes.ErrorController.accessDenied()))
      case user: UserAuthorised => {
        if (checkEnrolmentStates) {
          user.enrolmentState match {
            case TaxEnrolmentPending => Future.successful(Redirect(routes.ApplicationSubmittedController.pending()))
            case TaxEnrolmentError => Future.successful(Redirect(routes.ApplicationSubmittedController.rejected()))
            case TaxEnrolmentSuccess => Future.successful(Redirect(routes.ApplicationSubmittedController.successful()))
            case TaxEnrolmentDoesNotExist => callback(s"${user.internalId}-lisa-registration")
          }
        }
        else {
          callback(s"${user.internalId}-lisa-registration")
        }
      }
    } recover {
      case NonFatal(ex: Throwable) => {
        Logger.warn(s"Auth error: ${ex.getMessage}")
        Redirect(routes.ErrorController.error())
      }
    }
  }

  def hasAllSubmissionData(cacheId: String)(callback: (LisaRegistration) => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    // get organisation details
    cache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
      case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
      case Some(busData) => {

        cache.fetchAndGetEntry[OrganisationDetails](cacheId, OrganisationDetails.cacheKey).flatMap {
          case None => Future.successful(Redirect(routes.OrganisationDetailsController.get()))
            case Some(orgData) => {

            // get trading details
            cache.fetchAndGetEntry[TradingDetails](cacheId, TradingDetails.cacheKey).flatMap {
              case None => Future.successful(Redirect(routes.TradingDetailsController.get()))
                case Some(tradData) => {

                // get business structure

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
