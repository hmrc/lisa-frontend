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

import config.{FrontendAuthConnector, LisaShortLivedCache}
import models._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, _}
import play.api.{Logger,Configuration, Environment, Play}
import services.{AuthorisationService, RosmService}

import scala.concurrent.Future

trait OrganisationDetailsController extends LisaBaseController {
  val rosmService:RosmService

  def businessLables(businessStructure:  Option[BusinessStructure]): String = {
    val acceptableValues = Map("Corporate Body" -> "Corporation Tax reference number",
    "Limited Liability Partnership" -> "Partnership Unique Tax reference number"
    )

    businessStructure match {
      case None => {
        throw new NoBusinessStructureException
      }
      case Some(_) => {
        try {
          acceptableValues(businessStructure.get.businessStructure)
        }
        catch {
          case e: Exception => throw new Exception("Invalid business structure")
        }
      }
    }
  }

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      cache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap { bStructure =>
        cache.fetchAndGetEntry[OrganisationDetails](cacheId, OrganisationDetails.cacheKey).map { org =>
          try {
            org match {
              case Some(data) => Ok(views.html.registration.organisation_details(OrganisationDetails.form.fill(data), businessLables(bStructure)))
              case None => Ok(views.html.registration.organisation_details(OrganisationDetails.form, businessLables(bStructure)))
            }
          } catch {
            case e: NoBusinessStructureException => Redirect(routes.BusinessStructureController.get())
          }
        }
      }

    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      OrganisationDetails.form.bindFromRequest.fold(
        formWithErrors => {
          cache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap { bStructure =>
            Future.successful(BadRequest(views.html.registration.organisation_details(formWithErrors, businessLables(bStructure))))
          }
        },
        data => {
          cache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap { bStructure =>
            rosmService.rosmRegister(bStructure.get.businessStructure, data).flatMap {
              case Right(safeId) => {Logger.debug("rosmRegister Successful")
                cache.cache[OrganisationDetails](cacheId, OrganisationDetails.cacheKey,data.copy(safeId = Some(safeId)))
                handleRedirect(routes.TradingDetailsController.get().url)}
              case Left(error) => {Logger.error(s"rosmRegister Failure due to ${error}")
                Future.successful(BadRequest(views.html.registration.organisation_details(OrganisationDetails.form.withError("registerError", "Registration Failed"),businessLables(bStructure))))
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
  override val cache = LisaShortLivedCache
  override val rosmService = RosmService

  override val authorisationService = AuthorisationService
}

class NoBusinessStructureException extends Exception