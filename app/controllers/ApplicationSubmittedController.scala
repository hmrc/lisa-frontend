/*
 * Copyright 2024 HM Revenue & Customs
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
import models.ApplicationSent
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment}
import repositories.LisaCacheRepository
import services.AuthorisationService
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

class ApplicationSubmittedController @Inject()(
  implicit val sessionCacheRepository: LisaCacheRepository,
  implicit val env: Environment,
  implicit val config: Configuration,
  implicit val authorisationService: AuthorisationService,
  implicit val appConfig: AppConfig,
  override implicit val messagesApi: MessagesApi,
  override implicit val ec: ExecutionContext,
  implicit val messagesControllerComponents: MessagesControllerComponents,
  applicationSubmittedView: views.html.registration.application_submitted,
  applicationPendingView: views.html.registration.application_pending,
  applicationSuccessfulView: views.html.registration.application_successful,
  applicationRejectedView: views.html.registration.application_rejected
  ) extends LisaBaseController(messagesControllerComponents: MessagesControllerComponents, ec: ExecutionContext) {

  def get(): Action[AnyContent] = Action.async { implicit request =>
    logger.info("[ApplicationSubmittedController][get]")
    authorisedForLisa(_ => {
      sessionCacheRepository.getFromSession[ApplicationSent](DataKey(ApplicationSent.cacheKey)).map {
        case Some(application) =>
          Ok(applicationSubmittedView(application.email, application.subscriptionId, appConfig.displayURBanner))
      }
    }, checkEnrolmentState = false)
  }

  def pending(): Action[AnyContent] = Action.async { implicit request =>
    logger.info("[ApplicationSubmittedController][pending]")
    authorisedForLisa(_ => {
      Future.successful(Ok(applicationPendingView()))
    }, checkEnrolmentState = false)
  }

  def successful(): Action[AnyContent] = Action.async { implicit request =>
    logger.info("[ApplicationSubmittedController][successful]")
    authorisedForLisa(_ => {
      sessionCacheRepository.getFromSession[String](DataKey("lisaManagerReferenceNumber")).flatMap {
        case Some(lisaManagerReferenceNumber) =>
          Future.successful(Ok(applicationSuccessfulView(lisaManagerReferenceNumber)))
      }
    }, checkEnrolmentState = false)
  }

  def rejected(): Action[AnyContent] = Action.async { implicit request =>
    logger.info("[ApplicationSubmittedController][rejected]")
    authorisedForLisa(_ => {
      Future.successful(Ok(applicationRejectedView()))
    }, checkEnrolmentState = false)
  }
}
