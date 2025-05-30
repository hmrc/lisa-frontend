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
import models.{BusinessStructure, OrganisationDetails}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment, Logging}
import repositories.LisaCacheRepository
import services.{AuditService, AuthorisationService, RosmService}
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

class OrganisationDetailsController @Inject()(
                                               implicit val sessionCacheRepository: LisaCacheRepository,
                                               implicit val env: Environment,
                                               implicit val config: Configuration,
                                               implicit val authorisationService: AuthorisationService,
                                               implicit val rosmService: RosmService,
                                               implicit val auditService: AuditService,
                                               implicit val appConfig: AppConfig,
                                               override implicit val messagesApi: MessagesApi,
                                               override implicit val ec: ExecutionContext,
                                               implicit val messagesControllerComponents: MessagesControllerComponents,
                                               organisationDetailsView: views.html.registration.organisation_details
                                             ) extends LisaBaseController(messagesControllerComponents: MessagesControllerComponents, ec: ExecutionContext) with Logging {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { userId =>
      sessionCacheRepository.getFromSession[BusinessStructure](DataKey(BusinessStructure.cacheKey)).flatMap {
        case None => Future.successful(Redirect(routes.BusinessStructureController.get))
        case Some(businessStructure) =>
          val isPartnership = businessStructure.businessStructure == "LLP"
          val orgDetailsForm: Form[OrganisationDetails] = if (isPartnership) {
            OrganisationDetails.partnershipForm
          } else {
            OrganisationDetails.form
          }
          sessionCacheRepository.getFromSession[OrganisationDetails](DataKey(OrganisationDetails.cacheKey)).map {
            case Some(data) =>
              Ok(organisationDetailsView(
                createPostCall,
                orgDetailsForm.fill(data),
                isPartnership
              ))
            case None =>
              Ok(organisationDetailsView(
                createPostCall,
                orgDetailsForm,
                isPartnership
              ))
          }
      }
    }
  }

  val post: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { cacheId =>

      sessionCacheRepository.getFromSession[BusinessStructure](DataKey(BusinessStructure.cacheKey)).flatMap {
        case None => Future.successful(Redirect(routes.BusinessStructureController.get))
        case Some(businessStructure) =>
          val isPartnership = businessStructure.businessStructure == "LLP"
          val form = if (isPartnership) OrganisationDetails.partnershipForm else OrganisationDetails.form

          form.bindFromRequest().fold(
            formWithErrors => {
              logger.info("[OrganisationDetailsController][Post] form errors")
              Future.successful(
                BadRequest(organisationDetailsView(createPostCall, formWithErrors, isPartnership))
              )
            },
            data => {
              sessionCacheRepository.putSession[OrganisationDetails](DataKey(OrganisationDetails.cacheKey), data).flatMap { _ =>
                logger.info(s"[OrganisationDetailsController][Post] BusinessStructure retrieved: ${businessStructure.businessStructure}")
                rosmService.rosmRegister(businessStructure, data).flatMap {
                  case Right(safeId: String) =>
                    auditService.audit(auditType = "BusinessStructureSuccess",
                      path = routes.OrganisationDetailsController.post.url,
                      auditData = Map("companyName" -> data.companyName,
                        "ctrNumber" -> data.ctrNumber,
                        "businessStructure" -> businessStructure.businessStructure
                      )
                    )

                    logger.info("[OrganisationDetailsController][Post] rosmRegister Successful")
                    sessionCacheRepository.putSession[String](DataKey("safeId"), safeId).flatMap { _ =>
                      handleRedirect(routes.TradingDetailsController.get.url)
                    }
                  case Left(error) =>
                    logger.error(s"[OrganisationDetailsController][Post] Failure due to $error")
                    auditService.audit(auditType = "BusinessStructureFailed",
                      path = routes.OrganisationDetailsController.post.url,
                      auditData = Map("error" -> error)
                    )
                    handleRedirect(routes.MatchingFailedController.get.url)
                }
              }
            }
          )
      }
    }
  }

}
