/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment}
import repositories.LisaCacheRepository
import services.AuthorisationService
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

class YourDetailsController @Inject()(
                                       implicit val sessionCacheRepository: LisaCacheRepository,
                                       implicit val env: Environment,
                                       implicit val config: Configuration,
                                       implicit val authorisationService: AuthorisationService,
                                       implicit val appConfig: AppConfig,
                                       override implicit val messagesApi: MessagesApi,
                                       override implicit val ec: ExecutionContext,
                                       implicit val messagesControllerComponents: MessagesControllerComponents,
                                       yourDetailsView: views.html.registration.your_details
                                     ) extends LisaBaseController(messagesControllerComponents: MessagesControllerComponents, ec: ExecutionContext) {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { _ =>
      sessionCacheRepository.getFromSession[YourDetails](DataKey(YourDetails.cacheKey)).map {
        case Some(data) => Ok(yourDetailsView(createPostCall, YourDetails.form.fill(data)))
        case None => Ok(yourDetailsView(createPostCall, YourDetails.form))
      }

    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { cacheId =>

      YourDetails.form.bindFromRequest().fold(
        formWithErrors => {
          logger.info("[YourDetailsController][POST] form errors")
          Future.successful(BadRequest(yourDetailsView(createPostCall, formWithErrors)))
        },
        data => {
          logger.info("[YourDetailsController][POST] Successful")
          sessionCacheRepository.putSession[YourDetails](DataKey(YourDetails.cacheKey), data).flatMap { _ =>
            handleRedirect(routes.SummaryController.get.url)
          }
        }
      )

    }
  }
}
