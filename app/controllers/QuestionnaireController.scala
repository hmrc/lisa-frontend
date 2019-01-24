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
import models.Questionnaire
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment}
import services.{AuditService, AuthorisationService}
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache}

import scala.concurrent.Future

class QuestionnaireController @Inject()(
  implicit val sessionCache: SessionCache,
  implicit val shortLivedCache: ShortLivedCache,
  implicit val env: Environment,
  implicit val config: Configuration,
  implicit val authorisationService: AuthorisationService,
  implicit val auditService: AuditService,
  implicit val appConfig: AppConfig,
  implicit val messages: Messages
) extends LisaBaseController {

  def showQuestionnaire: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.feedback.feedbackQuestionnaire(Questionnaire.form)))
  }

  def submitQuestionnaire: Action[AnyContent] = Action.async {
    implicit request =>
      Questionnaire.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.feedback.feedbackQuestionnaire(formWithErrors))
          )
        },
        data => {
          auditService.audit(auditType="lisaFeedbackSurvey", path=routes.QuestionnaireController.submitQuestionnaire().url,createQuestionnaireAudit(data))
          Future.successful(Redirect(routes.QuestionnaireController.feedbackThankyou()))
        }
      )
  }

  def feedbackThankyou: Action[AnyContent] = Action.async {
    implicit request =>
    Future.successful(Ok(views.html.feedback.thanks()))
  }

  private def createQuestionnaireAudit(survey: Questionnaire): Map[String, String] = {
    Map(
      "satisfactionLevel" -> survey.satisfactionLevel.mkString,
      "whyGiveThisRating" -> survey.whyGiveThisRating.mkString
    )
  }
}