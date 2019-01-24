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

package controllers

import com.google.inject.Inject
import config.AppConfig
import models.{BusinessStructure, OrganisationDetails}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment, Logger}
import services.{AuthorisationService, RosmService}
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

import scala.concurrent.Future

class OrganisationDetailsController @Inject()(
  implicit val sessionCache: SessionCache,
  implicit val shortLivedCache: ShortLivedCache,
  implicit val env: Environment,
  implicit val config: Configuration,
  implicit val authorisationService: AuthorisationService,
  implicit val rosmService: RosmService,
  implicit val appConfig: AppConfig,
  implicit val messages: Messages
) extends LisaBaseController {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      shortLivedCache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
        case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
        case Some(businessStructure) => {
          val orgDetailsForm: Form[OrganisationDetails] = if (isPartnership(businessStructure)) {
            OrganisationDetails.partnershipForm
          } else {
            OrganisationDetails.form
          }
          shortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, OrganisationDetails.cacheKey).map {
            case Some(data) =>
              Ok(views.html.registration.organisation_details(
                orgDetailsForm.fill(data),
                isPartnership(businessStructure)
              ))
            case None =>
              Ok(views.html.registration.organisation_details(
                orgDetailsForm,
                isPartnership(businessStructure)
              ))
          }
        }
      }
    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      shortLivedCache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
        case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
        case Some(businessStructure) => {
          val form = if (isPartnership(businessStructure)) OrganisationDetails.partnershipForm else OrganisationDetails.form

          form.bindFromRequest.fold(
            formWithErrors => {
              Future.successful(
                BadRequest(views.html.registration.organisation_details(formWithErrors, isPartnership(businessStructure)))
              )
            },
            data => {
              shortLivedCache.cache[OrganisationDetails](cacheId, OrganisationDetails.cacheKey, data).flatMap { _ =>
                Logger.debug(s"BusinessStructure retrieved: ${businessStructure.businessStructure}")
                rosmService.rosmRegister(businessStructure, data).flatMap {
                  case Right(safeId) => {
                    Logger.debug("rosmRegister Successful")
                    shortLivedCache.cache[String](cacheId, "safeId", safeId)
                    handleRedirect(routes.TradingDetailsController.get().url)
                  }
                  case Left(error) => {
                    Logger.error(s"OrganisationDetailsController: rosmRegister Failure due to $error")
                    handleRedirect(routes.MatchingFailedController.get().url)
                  }
                }
              }
            }
          )
        }
      }
    }
  }

  private def isPartnership(businessStructure: BusinessStructure): Boolean = {
    businessStructure.businessStructure == Messages("org.details.llp")
  }

}