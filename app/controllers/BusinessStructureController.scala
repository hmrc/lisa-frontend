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

class BusinessStructureController @Inject() (
  val sessionCacheRepository: LisaCacheRepository,
  val env: Environment,
  val config: Configuration,
  val authorisationService: AuthorisationService,
  override val messagesApi: MessagesApi,
//  implicit val appConfig: AppConfig,
  val messagesControllerComponents: MessagesControllerComponents,
  businessStructureView: views.html.registration.business_structure
)(using ec: ExecutionContext, appConfig: AppConfig)
    extends LisaBaseController(messagesControllerComponents) {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { _ =>
      sessionCacheRepository.getFromSession[BusinessStructure](DataKey(BusinessStructure.cacheKey)).map {
        case Some(data) => Ok(businessStructureView(createPostCall, BusinessStructure.form.fill(data)))
        case None       => Ok(businessStructureView(createPostCall, BusinessStructure.form))
      }
    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { _ =>
      BusinessStructure.form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            logger.warn("[BusinessStructureController][POST] Returning bad request due to form errors")
            Future.successful(BadRequest(businessStructureView(createPostCall, formWithErrors)))
          },
          (data: BusinessStructure) => {
            logger.info("[BusinessStructureController][POST] Successful")
            sessionCacheRepository.putSession[BusinessStructure](DataKey(BusinessStructure.cacheKey), data).flatMap { _ =>
              handleRedirect(routes.OrganisationDetailsController.get.url)
            }
          }
        )
    }
  }

}
