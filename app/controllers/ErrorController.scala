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
import config.{AppConfig, LisaSessionCache, LisaShortLivedCache}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment}
import services.AuthorisationService

import scala.concurrent.Future

class ErrorController @Inject()(
  val sessionCache: LisaSessionCache,
  val shortLivedCache: LisaShortLivedCache,
  val env: Environment,
  val config: Configuration,
  val authorisationService: AuthorisationService,
  implicit val appConfig: AppConfig,
  implicit val messages: Messages
) extends LisaBaseController {

  val accessDenied: Action[AnyContent] = Action.async { implicit request =>
    val loginUrl = ggLoginUrl + "?origin=lisa-api&continue=" + routes.OrganisationDetailsController.get().url
    val registerUrl = appConfig.getSignOutUrl(appConfig.registerOrgUrl)

    Future.successful(Forbidden(views.html.error.access_denied(loginUrl, registerUrl)))
  }

}