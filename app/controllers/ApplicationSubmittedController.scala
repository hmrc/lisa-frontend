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
import models.ApplicationSent
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment}
import services.AuthorisationService
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

import scala.concurrent.Future

class ApplicationSubmittedController @Inject()(
  implicit val sessionCache: SessionCache,
  implicit val shortLivedCache: ShortLivedCache,
  implicit val env: Environment,
  implicit val config: Configuration,
  implicit val authorisationService: AuthorisationService,
  implicit val appConfig: AppConfig,
  implicit val messagesApi: MessagesApi
) extends LisaBaseController {

  def get(): Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa((_) => {
      sessionCache.fetchAndGetEntry[ApplicationSent](ApplicationSent.cacheKey).map {
        case Some(application) => {
          Ok(views.html.registration.application_submitted(application.email, application.subscriptionId))
        }
      }
    }, checkEnrolmentState = false)
  }

  def pending(): Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa((_) => {
      Future.successful(Ok(views.html.registration.application_pending()))
    }, checkEnrolmentState = false)
  }

  def successful(): Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa((_) => {
      sessionCache.fetchAndGetEntry[String]("lisaManagerReferenceNumber").flatMap {
        case Some(lisaManagerReferenceNumber) =>
          Future.successful(Ok(views.html.registration.application_successful(lisaManagerReferenceNumber)))
      }
    }, checkEnrolmentState = false)
  }

  def rejected(): Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa((_) => {
      Future.successful(Ok(views.html.registration.application_rejected()))
    }, checkEnrolmentState = false)
  }
}