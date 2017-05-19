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
import models._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.Future

trait BusinessStructureController extends LisaBaseController {

  val cache:ShortLivedCache

  private val cacheKey = "businessStructure"

  private val form = Form(
    mapping(
      "businessStructure" -> nonEmptyText
    )(BusinessStructure.apply)(BusinessStructure.unapply)
  )

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      cache.fetchAndGetEntry[BusinessStructure](cacheId, cacheKey).map {
        case Some(data) => Ok(views.html.registration.business_structure(form.fill(data)))
        case None => Ok(views.html.registration.business_structure(form))
      }
    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.business_structure(formWithErrors)))
        },
        data => {
          cache.cache[BusinessStructure](cacheId, cacheKey, data)

          Future.successful(Redirect(routes.YourDetailsController.get()))
        }
      )
    }
  }

}

object BusinessStructureController extends BusinessStructureController {
  val authConnector = FrontendAuthConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val cache = LisaShortLivedCache
}