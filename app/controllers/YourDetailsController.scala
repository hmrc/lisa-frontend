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
import models.*
import play.api.i18n.MessagesApi
import play.api.mvc.*
import play.api.{Configuration, Environment}
import repositories.LisaCacheRepository
import services.AuthorisationService
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

class YourDetailsController @Inject() (
  val sessionCacheRepository: LisaCacheRepository,
  val env: Environment,
  val config: Configuration,
  val authorisationService: AuthorisationService,
  override val messagesApi: MessagesApi,
  implicit val appConfig: AppConfig,
  val messagesControllerComponents: MessagesControllerComponents,
  yourDetailsView: views.html.registration.your_details
)(using ec: ExecutionContext)
    extends LisaBaseController(messagesControllerComponents) {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { _ =>
      sessionCacheRepository.getFromSession[YourDetails](DataKey(YourDetails.cacheKey)).map {
        case Some(data) => Ok(yourDetailsView(createPostCall, YourDetails.form.fill(data)))
        case None       => Ok(yourDetailsView(createPostCall, YourDetails.form))
      }

    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { _ =>
      YourDetails.form
        .bindFromRequest()
        .fold(
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
