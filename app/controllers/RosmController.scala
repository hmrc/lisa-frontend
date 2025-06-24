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
import connectors.{EmailConnector, RosmJsonFormats}

import java.time.format.DateTimeFormatter
import java.time.LocalDate
import models.{ApplicationSent, LisaRegistration}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment, Logging}
import repositories.LisaCacheRepository
import services.{AuditService, AuthorisationService, RosmService}
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

class RosmController @Inject()(
  implicit val sessionCacheRepository: LisaCacheRepository,
  implicit val env: Environment,
  implicit val config: Configuration,
  implicit val authorisationService: AuthorisationService,
  implicit val auditService: AuditService,
  implicit val rosmService: RosmService,
  implicit val emailConnector: EmailConnector,
  implicit val appConfig: AppConfig,
  override implicit val messagesApi: MessagesApi,
  override implicit val ec: ExecutionContext,
  implicit val messagesControllerComponents: MessagesControllerComponents,
  errorView: views.html.error_template
) extends LisaBaseController(messagesControllerComponents: MessagesControllerComponents, ec: ExecutionContext) with RosmJsonFormats with Logging {

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { cacheId =>
      hasAllSubmissionData() { registrationDetails =>
        rosmService.performSubscription(registrationDetails).flatMap {
          case Right(subscriptionId) =>
            logger.info(s"[RosmController][POST] Audit of Submission -> auditType = applicationReceived subscriptionId: $subscriptionId" )

            auditService.audit(auditType = "applicationReceived",
              path = routes.RosmController.post.url,
              auditData = createAuditDetails(registrationDetails) ++ Map("subscriptionId" -> subscriptionId))

            val applicationSentVM: ApplicationSent = ApplicationSent(subscriptionId = subscriptionId, email = registrationDetails.yourDetails.email)

            sessionCacheRepository.putSession[ApplicationSent](DataKey(ApplicationSent.cacheKey), applicationSentVM).map { _ =>
              sessionCacheRepository.deleteFromSession(DataKey(cacheId))

              emailConnector.sendTemplatedEmail(
                emailAddress = applicationSentVM.email,
                templateName = "lisa_application_submit",
                params = Map(
                  "application_reference" -> applicationSentVM.subscriptionId,
                  "email" -> applicationSentVM.email,
                  "review_date" -> LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("d MMMM y")),
                  "first_name" -> registrationDetails.yourDetails.firstName,
                  "last_name" -> registrationDetails.yourDetails.lastName
                )
              )

              Redirect(routes.ApplicationSubmittedController.get)
            }
          case Left(error) =>
            logger.error(s"[RosmController][POST] Audit of Submission -> auditType = applicationNotReceived failed with $error")

            auditService.audit(auditType = "applicationNotReceived",
              path = routes.RosmController.post.url,
              auditData = createAuditDetails(registrationDetails) ++ Map("reasonNotReceived" -> error))

            Future.successful(InternalServerError(errorView()))
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
