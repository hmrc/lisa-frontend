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

import config.{FrontendAuthConnector, LisaSessionCache, LisaShortLivedCache}
import models.OrganisationDetails._
import models._
import play.api.Play.current
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment, Logger, Play}
import services.{AuthorisationService, RosmService}

import scala.concurrent.Future

trait OrganisationDetailsController extends LisaBaseController {
  val rosmService:RosmService

  private def businessLabels(businessStructure:  BusinessStructure): String = {
    val acceptableValues = Map(
      Messages("org.details.corpbody") -> "Corporation Tax reference number",
      Messages("org.details.llp") -> "Partnership Unique Tax reference number"
    )

    acceptableValues(businessStructure.businessStructure)
  }

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      shortLivedCache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
        case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
        case Some(businessStructure) => {
          shortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, OrganisationDetails.cacheKey).map {
            case Some(data) => Ok(views.html.registration.organisation_details(OrganisationDetails.form.fill(data), businessLabels(businessStructure)))
            case None => Ok(views.html.registration.organisation_details(OrganisationDetails.form, businessLabels(businessStructure)))
          }
        }
      }
    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      OrganisationDetails.form.bindFromRequest.fold(
        formWithErrors => {
          shortLivedCache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
            case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
            case Some(businessStructure) => {
              Future.successful(BadRequest(views.html.registration.organisation_details(formWithErrors, businessLabels(businessStructure))))
            }
          }
        },
        data => {
          shortLivedCache.cache[OrganisationDetails](cacheId, OrganisationDetails.cacheKey, data)

          shortLivedCache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
            case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
            case Some(businessStructure) => {
              Logger.debug("BusinessStructure retrieved")
              rosmService.rosmRegister(businessStructure, data).flatMap {
                case Right(safeId) => {
                  Logger.debug("rosmRegister Successful")
                  shortLivedCache.cache[String](cacheId, "safeId", safeId)
                  handleRedirect(routes.TradingDetailsController.get().url)
                }
                case Left(error) => {
                  Logger.error(s"OrganisationDetailsController: rosmRegister Failure due to $error")

                  val regErrors = Seq(
                    FormError(businessStructure.businessStructure, ""),
                    FormError("companyName", Messages("error.companyName")),
                    FormError("ctrNumber", Messages("error.ctrNumber"))
                  )

                  Future.successful(BadRequest(views.html.registration.organisation_details(
                    OrganisationDetails.form.copy(errors = regErrors) fill (data), businessLabels(businessStructure))))
                }
              }
            }
          }
        }
      )
    }
  }
}

object OrganisationDetailsController extends OrganisationDetailsController {
  val authConnector = FrontendAuthConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val sessionCache = LisaSessionCache
  override val shortLivedCache = LisaShortLivedCache
  override val rosmService = RosmService

  override val authorisationService = AuthorisationService
}