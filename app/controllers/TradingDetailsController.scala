/*
 * Copyright 2021 HM Revenue & Customs
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
import models.TradingDetails
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

import scala.concurrent.{ExecutionContext, Future}

class TradingDetailsController @Inject()(
  implicit val sessionCache: SessionCache,
  implicit val shortLivedCache: ShortLivedCache,
  implicit val env: Environment,
  implicit val config: Configuration,
  implicit val authorisationService: AuthorisationService,
  implicit val appConfig: AppConfig,
  override implicit val messagesApi: MessagesApi,
  override implicit val ec: ExecutionContext,
  implicit val messagesControllerComponents: MessagesControllerComponents,
  tradingDetailsView: views.html.registration.trading_details
) extends LisaBaseController(messagesControllerComponents: MessagesControllerComponents, ec: ExecutionContext) {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { cacheId =>
      shortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, TradingDetails.cacheKey).map {
        case Some(data) => Ok(tradingDetailsView(TradingDetails.form.fill(data)))
        case None => Ok(tradingDetailsView(TradingDetails.form))
      }
    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { cacheId =>
      TradingDetails.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(tradingDetailsView(formWithErrors)))
        },
        data => {
          shortLivedCache.cache[TradingDetails](cacheId, TradingDetails.cacheKey, TradingDetails.uppercaseZ(data)).flatMap { _ =>
            handleRedirect(routes.YourDetailsController.get.url)
          }
        }
      )
    }
  }

}