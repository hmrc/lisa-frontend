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
import models._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment}
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

import scala.concurrent.Future

class YourDetailsController @Inject()(
  implicit val sessionCache: SessionCache,
  implicit val shortLivedCache: ShortLivedCache,
  implicit val env: Environment,
  implicit val config: Configuration,
  implicit val authorisationService: AuthorisationService,
  implicit val appConfig: AppConfig,
  implicit val messagesApi: MessagesApi
) extends LisaBaseController {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      shortLivedCache.fetchAndGetEntry[YourDetails](cacheId, YourDetails.cacheKey).map {
        case Some(data) => Ok(views.html.registration.your_details(YourDetails.form.fill(data)))
        case None => Ok(views.html.registration.your_details(YourDetails.form))
      }

    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      YourDetails.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.your_details(formWithErrors)))
        },
        data => {
          shortLivedCache.cache[YourDetails](cacheId, YourDetails.cacheKey, data).flatMap { _ =>
            handleRedirect(routes.SummaryController.get().url)
          }
        }
      )

    }
  }

}
