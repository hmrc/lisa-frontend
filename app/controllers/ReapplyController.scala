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

import config.{LisaSessionCache, LisaShortLivedCache}
import models.UserAuthorised
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment, Play}
import services.AuthorisationService

trait ReapplyController extends LisaBaseController {
  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisationService.userStatus flatMap {
      case user: UserAuthorised => {
        shortLivedCache.cache[Boolean](s"${user.internalId}-lisa-registration","reapplication",true) map { res =>
         Redirect(routes.BusinessStructureController.get())
        }
      }
    }
  }
}

object ReapplyController extends ReapplyController {
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val sessionCache = LisaSessionCache
  override val shortLivedCache = LisaShortLivedCache
  override val authorisationService = AuthorisationService
}
