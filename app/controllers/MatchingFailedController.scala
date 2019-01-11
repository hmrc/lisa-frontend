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

import config.{LisaSessionCache, LisaShortLivedCache}
import models.BusinessStructure
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.{Configuration, Environment, Play}
import play.api.mvc.{Action, AnyContent}
import services.AuthorisationService

import scala.concurrent.Future

trait MatchingFailedController extends LisaBaseController {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      shortLivedCache.fetchAndGetEntry[BusinessStructure](cacheId, BusinessStructure.cacheKey).flatMap {
        case None => Future.successful(Redirect(routes.BusinessStructureController.get()))
        case Some(businessStructure) => {
          val isPartnership = (businessStructure.businessStructure == Messages("org.details.llp"))

          Future.successful(Ok(views.html.registration.matching_failed(isPartnership)))
        }
      }
    }
  }

}

object MatchingFailedController extends MatchingFailedController {
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val sessionCache = LisaSessionCache
  override val shortLivedCache = LisaShortLivedCache
  override val authorisationService = AuthorisationService
}
