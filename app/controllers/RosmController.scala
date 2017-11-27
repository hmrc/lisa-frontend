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

import config.{LisaSessionCache, LisaShortLivedCache}
import connectors.{EmailConnector, RosmJsonFormats}
import models.{ApplicationSent, LisaRegistration}
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment, Logger, Play}
import services.{AuditService, AuthorisationService, RosmService}
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

trait RosmController extends LisaBaseController
  with RosmJsonFormats {

  val auditService:AuditService
  val rosmService:RosmService
  val emailConnector: EmailConnector

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      hasAllSubmissionData(cacheId) { registrationDetails =>
        rosmService.performSubscription(registrationDetails).flatMap {
          case Right(subscriptionId) => {
            Logger.info("Audit of Submission -> auditType = applicationReceived" + subscriptionId)

            auditService.audit(auditType = "applicationReceived",
              path = routes.RosmController.get().url,
              auditData = createAuditDetails(registrationDetails) ++ Map("subscriptionId" -> subscriptionId))

            val applicationSentVM = ApplicationSent(subscriptionId = subscriptionId, email = registrationDetails.yourDetails.email)

            sessionCache.cache[ApplicationSent](ApplicationSent.cacheKey, applicationSentVM).map { _ =>
              shortLivedCache.remove(cacheId)

              emailConnector.sendTemplatedEmail(
                emailAddress = applicationSentVM.email,
                templateName = "lisa_application_submit",
                params = Map(
                  "application_reference" -> applicationSentVM.subscriptionId,
                  "email" -> applicationSentVM.email
                )
              )

              Redirect(routes.ApplicationSubmittedController.get())
            }
          }
          case Left(error) => {
            Logger.info("Audit of Submission -> auditType = applicationNotReceived")

            auditService.audit(auditType = "applicationNotReceived",
              path = routes.RosmController.get().url,
              auditData = createAuditDetails(registrationDetails) ++ Map("reasonNotReceived" -> error))

            Future.successful(InternalServerError(views.html.error_template(
              Messages("global.error.InternalServerError500.title"),
              Messages("global.error.InternalServerError500.heading"),
              Messages("global.error.InternalServerError500.message"))))
          }
        }
      }
    }
  }

  private def createAuditDetails(registrationDetails: LisaRegistration) = {
    Map(
      "companyName" -> registrationDetails.organisationDetails.companyName,
      "utr" -> registrationDetails.organisationDetails.ctrNumber,
      "financialServicesRegisterReferenceNumber" -> registrationDetails.tradingDetails.fsrRefNumber,
      "isaProviderReferenceNumber" -> registrationDetails.tradingDetails.isaProviderRefNumber,
      "firstName" -> registrationDetails.yourDetails.firstName,
      "lastName" -> registrationDetails.yourDetails.lastName,
      "roleInOrganisation" -> registrationDetails.yourDetails.role,
      "phoneNumber" -> registrationDetails.yourDetails.phone,
      "emailAddress" -> registrationDetails.yourDetails.email
    )
  }

}

object RosmController extends RosmController {
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val sessionCache = LisaSessionCache
  override val shortLivedCache = LisaShortLivedCache
  override val auditService = AuditService
  override val rosmService = RosmService
  override val authorisationService = AuthorisationService
  override val emailConnector = EmailConnector
}
