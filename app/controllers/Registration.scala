/*
 * Copyright 2017 HM Revenue & Customs
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

import config.FrontendAuthConnector
import models.OrganisationDetails
import play.api.{Environment, Play}
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object Registration extends Registration {
  val authConnector = FrontendAuthConnector
  val config = Play.current.configuration
  val env = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
}

trait Registration extends FrontendController with AuthorisedFunctions with Redirects {

  implicit val organisationDetailsFormats = Json.format[OrganisationDetails]

  private val organisationForm = Form(
    mapping(
      "companyName" -> nonEmptyText,
      "ctrNumber" -> nonEmptyText
    )(OrganisationDetails.apply)(OrganisationDetails.unapply)
  )

  val organisationDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      Future.successful(Ok(views.html.registration.organisation_details(organisationForm)))
    } recoverWith {
      handleFailure
    }
  }

  val submitOrganisationDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      organisationForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.organisation_details(formWithErrors)))
        },
        data => {
          // at this point we need to submit / store the data
          // not implemented for now - to be done in future tasks / stories
          Future.successful(NotImplemented(Json.toJson[OrganisationDetails](data)))
        }
      )
    } recoverWith {
      handleFailure
    }
  }

  private def handleFailure(implicit request: Request[_]) = PartialFunction[Throwable, Future[Result]] {
    case _ : NoActiveSession => Future.successful(toGGLogin("/lifetime-isa/register/organisation-details"))
    case _ => Future.successful(Forbidden(views.html.error.access_denied()))
  }

}