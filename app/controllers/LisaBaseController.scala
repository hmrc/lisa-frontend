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

import config.AppConfig
import models._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Reads}
import play.api.mvc._
import repositories.LisaCacheRepository
import services.AuthorisationService
import uk.gov.hmrc.mongo.cache.{CacheItem, DataKey}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding

import scala.concurrent.{ExecutionContext, Future}

abstract class LisaBaseController(messagesControllerComponents: MessagesControllerComponents, implicit val ec: ExecutionContext)
  extends FrontendController(messagesControllerComponents: MessagesControllerComponents)
  with I18nSupport with Logging with WithUnsafeDefaultFormBinding {

  val appConfig: AppConfig
  val sessionCacheRepository: LisaCacheRepository
  val authorisationService: AuthorisationService

  def authorisedForLisa(callback: String => Future[Result], checkEnrolmentState: Boolean = true)
                       (implicit request: Request[AnyContent]): Future[Result] = {
    authorisationService.userStatus flatMap {
      case UserNotLoggedIn => Future.successful(toGGLogin(appConfig.loginCallback))
      case UserUnauthorised => Future.successful(Redirect(routes.ErrorController.accessDeniedIndividualOrAgent))
      case UserNotAdmin => Future.successful(Redirect(routes.ErrorController.accessDeniedAssistant))
      case user: UserAuthorisedAndEnrolled => handleUserAuthorisedAndEnrolled(callback, checkEnrolmentState, user)
      case user: UserAuthorised => handleUserAuthorised(callback, checkEnrolmentState, user)
    }
  }
  def toGGLogin(continueUrl: String): Result = {
    Redirect(
      appConfig.loginURL,
      Map(
        "continue_url" -> Seq(continueUrl),
        "origin"   -> Seq("lisa-frontend")
      )
    )
  }

  private def isReapplication(user: UserAuthorised)(implicit request: Request[AnyContent]): Future[Boolean] = {
    sessionCacheRepository.getFromSession[Boolean](DataKey(Reapplication.cacheKey))
      .map(_.getOrElse(false))
  }


  private def handleUserAuthorised(callback: String => Future[Result], checkEnrolmentState: Boolean, user: UserAuthorised)
                                  (implicit request: Request[AnyContent]): Future[Result] = {
    logger.debug("User Authorised")
    isReapplication(user) flatMap { isReapplication =>
      if (checkEnrolmentState && !isReapplication) {
        user.enrolmentState match {
          case TaxEnrolmentPending =>
            logger.debug("Enrollment Pending")
            Future.successful(Redirect(routes.ApplicationSubmittedController.pending))
          case TaxEnrolmentError =>
            logger.debug("Enrollment Rejected")
            Future.successful(Redirect(routes.ApplicationSubmittedController.rejected))
          case TaxEnrolmentDoesNotExist =>
            logger.debug("Enrollment Does Not Exist")
            callback(s"${user.internalId}-lisa-registration")
        }
      }
      else {
        callback(s"${user.internalId}-lisa-registration")
      }
    }
  }

  private def handleUserAuthorisedAndEnrolled(callback: String => Future[Result], checkEnrolmentState: Boolean, user: UserAuthorisedAndEnrolled)
                                             (implicit request: Request[AnyContent]): Future[Result] = {
    logger.debug("User Authorised And Enrolled")

    if (checkEnrolmentState) {
      sessionCacheRepository.putSession[String](DataKey("lisaManagerReferenceNumber"), user.lisaManagerReferenceNumber).map { _ =>
        Redirect(routes.ApplicationSubmittedController.successful)
      }
    }
    else {
      callback(s"${user.internalId}-lisa-registration")
    }
  }

  def hasAllSubmissionData()(callback: LisaRegistration => Future[Result])
                          (implicit request: Request[AnyContent]): Future[Result] = {
    sessionCacheRepository.getFullCache(request) flatMap {
      case Some(cache: CacheItem) =>
        val data = cache.data.value
        val cacheResult: Either[Result, LisaRegistration] = for {
          bs <- getOrRedirect[BusinessStructure](data, BusinessStructure.cacheKey, Redirect(routes.BusinessStructureController.get))
          od <- getOrRedirect[OrganisationDetails](data, OrganisationDetails.cacheKey, Redirect(routes.OrganisationDetailsController.get))
          sId <- getOrRedirect[String](data, "safeId", Redirect(routes.OrganisationDetailsController.get))
          td <- getOrRedirect[TradingDetails](data, TradingDetails.cacheKey, Redirect(routes.TradingDetailsController.get))
          yd <- getOrRedirect[YourDetails](data, YourDetails.cacheKey, Redirect(routes.YourDetailsController.get))
        } yield LisaRegistration(od, td, bs, yd, sId)
        cacheResult.fold(redirect => Future.successful(redirect), callback(_))
      case None => Future.successful(Redirect(routes.BusinessStructureController.get))
    }
  }

  private def getOrRedirect[T](cache: collection.Map[String, JsValue], key: String, redirect: Result)(implicit reads: Reads[T]): Either[Result, T] = {
    cache.get(key).map(_.as[T]).toRight(redirect)
  }

  def createPostCall(implicit request: MessagesRequest[AnyContent]): Call = {
    Call("POST", request.uri)
  }

  def handleRedirect(redirectUrl: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val returnUrl: Option[String] = request.getQueryString("returnUrl")

    returnUrl match {
      case Some(url) if url.matches("^\\/lifetime\\-isa\\/.*$") => Future.successful(Redirect(url))
      case _ => Future.successful(Redirect(redirectUrl))
    }
  }

}
