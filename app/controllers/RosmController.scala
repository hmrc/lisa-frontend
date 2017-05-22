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
import connectors.{RosmConnector, RosmJsonFormats}
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment, Play}

trait RosmController extends LisaBaseController
  with RosmJsonFormats {

  val rosmConnector:RosmConnector

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      hasAllSubmissionData(cacheId) { (registrationDetails) =>
        cache.remove(cacheId)

        registrationDetails.tradingDetails.ctrNumber match {
          case "0000000000" => Redirect(routes.ErrorController.error())
          case _ => Redirect(routes.ApplicationSubmittedController.get(registrationDetails.yourDetails.email))
        }
      }
    }
  }

}

object RosmController extends RosmController {
  val authConnector = FrontendAuthConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val rosmConnector = RosmConnector
  override val cache = LisaShortLivedCache
}