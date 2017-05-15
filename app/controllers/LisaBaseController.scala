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

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals.internalId
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait LisaBaseController extends FrontendController
  with AuthorisedFunctions
  with Redirects {

  def authorisedForLisa(callback: (String) => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    authorised(
      AffinityGroup.Organisation and AuthProviders(GovernmentGateway)
    ).retrieve(internalId) { id =>
      val userId = id.getOrElse(throw new RuntimeException("No internalId for logged in user"))

      callback(s"$userId-lisa-registration")
    } recoverWith {
      handleFailure
    }
  }

  def handleFailure(implicit request: Request[_]): PartialFunction[Throwable, Future[Result]] = PartialFunction[Throwable, Future[Result]] {
    case _ : NoActiveSession => Future.successful(toGGLogin("/lifetime-isa/register/organisation-details"))
    case _ : AuthorisationException => Future.successful(Redirect(routes.ErrorController.accessDenied()))
    case _ => Future.successful(Redirect(routes.ErrorController.error()))
  }

}