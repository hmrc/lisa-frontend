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
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class ErrorController @Inject() (
  val config: Configuration,
  val env: Environment,
  override val messagesApi: MessagesApi,
  val messagesControllerComponents: MessagesControllerComponents,
  accessDeniedIndividualOrAgentView: views.html.error.access_denied_individual_or_agent,
  accessDeniedAssistantView: views.html.error.access_denied_assistant
)(using ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(messagesControllerComponents) {

  val accessDeniedIndividualOrAgent: Action[AnyContent] = Action.async { implicit request =>
    val loginUrl    = appConfig.loginURL + "?origin=lisa-api&continue=" + routes.OrganisationDetailsController.get.url
    val registerUrl = appConfig.getSignOutUrl(appConfig.registerOrgUrl)

    Future.successful(Forbidden(accessDeniedIndividualOrAgentView(loginUrl, registerUrl)))
  }

  val accessDeniedAssistant: Action[AnyContent] = Action.async { implicit request =>
    val loginUrl    = appConfig.loginURL + "?origin=lisa-api&continue=" + routes.OrganisationDetailsController.get.url
    val registerUrl = appConfig.getSignOutUrl(appConfig.registerOrgUrl)

    Future.successful(Forbidden(accessDeniedAssistantView(loginUrl, registerUrl)))
  }

}
