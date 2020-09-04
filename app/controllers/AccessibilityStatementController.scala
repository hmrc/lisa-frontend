/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class AccessibilityStatementController @Inject()(
  implicit val config: Configuration,
  implicit val env: Environment,
  override implicit val messagesApi: MessagesApi,
  implicit val appConfig: AppConfig,
  implicit val ec: ExecutionContext,
  implicit val messagesControllerComponents: MessagesControllerComponents
) extends FrontendController(messagesControllerComponents: MessagesControllerComponents) with AuthRedirects with I18nSupport {

  def onPageLoad(problemPageUri: String): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.accessibility_statement_view(problemPageUri)))
  }
}
