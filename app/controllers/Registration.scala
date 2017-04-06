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

import config.{FrontendAuthConnector, ShortLivedCache}
import connectors.{RosmConnector, RosmJsonFormats}
import models._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait Registration extends FrontendController
  with AuthorisedFunctions
  with Redirects
  with RosmJsonFormats {

  val rosmConnector:RosmConnector
  val cache:ShortLivedCache

  val organisationDetails: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      cache.fetchAndGetEntry[OrganisationDetails](cacheId, organisationDetailsCacheKey).map {
        case Some(data) => Ok(views.html.registration.organisation_details(organisationForm.fill(data)))
        case None => Ok(views.html.registration.organisation_details(organisationForm))
      }

    }
  }

  val submitOrganisationDetails: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      organisationForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.organisation_details(formWithErrors)))
        },
        data => {
          ShortLivedCache.cache[OrganisationDetails](cacheId, organisationDetailsCacheKey, data)

          Future.successful(Redirect(routes.Registration.tradingDetails()))
        }
      )

    }
  }

  val tradingDetails: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      ShortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, tradingDetailsCacheKey).map {
        case Some(data) => Ok(views.html.registration.trading_details(tradingForm.fill(data)))
        case None => Ok(views.html.registration.trading_details(tradingForm))
      }

    }
  }

  val submitTradingDetails: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      tradingForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.trading_details(formWithErrors)))
        },
        data => {
          ShortLivedCache.cache[TradingDetails](cacheId, tradingDetailsCacheKey, data)

          Future.successful(Redirect(routes.Registration.yourDetails()))
        }
      )

    }
  }

  val yourDetails: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      ShortLivedCache.fetchAndGetEntry[YourDetails](cacheId, yourDetailsCacheKey).map {
        case Some(data) => Ok(views.html.registration.your_details(yourForm.fill(data)))
        case None => Ok(views.html.registration.your_details(yourForm))
      }

    }
  }

  val submitYourDetails: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      yourForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.your_details(formWithErrors)))
        },
        data => {
          ShortLivedCache.cache[YourDetails](cacheId, yourDetailsCacheKey, data)

          Future.successful(Redirect(routes.Registration.summary()))
        }
      )

    }
  }

  val summary: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      // get organisation details
      ShortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, organisationDetailsCacheKey).flatMap {
        case None => Future.successful(Redirect(routes.Registration.organisationDetails()))
        case Some(orgData) => {

          // get trading details
          ShortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, tradingDetailsCacheKey).flatMap {
            case None => Future.successful(Redirect(routes.Registration.tradingDetails()))
            case Some(tradData) => {

              // get user details
              ShortLivedCache.fetchAndGetEntry[YourDetails](cacheId, yourDetailsCacheKey).map {
                case None => Redirect(routes.Registration.yourDetails())
                case Some(yourData) => {
                  Ok(views.html.registration.summary(new LisaRegistration(orgData, tradData, yourData)))
                }
              }
            }
          }
        }
      }
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      // get organisation details
      ShortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, organisationDetailsCacheKey).flatMap {
        case None => Future.successful(Redirect(routes.Registration.organisationDetails()))
        case Some(orgData) => {

          // get trading details
          ShortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, tradingDetailsCacheKey).flatMap {
            case None => Future.successful(Redirect(routes.Registration.tradingDetails()))
            case Some(tradData) => {

              // get user details
              ShortLivedCache.fetchAndGetEntry[YourDetails](cacheId, yourDetailsCacheKey).flatMap {
                case None => Future.successful(Redirect(routes.Registration.yourDetails()))
                case Some(yourData) => {
                  val registrationDetails = LisaRegistration(orgData, tradData, yourData)

                  ShortLivedCache.remove(cacheId)

                  val rosmReg = RosmRegistration(regime = "LISA", requiresNameMatch = false, isAnAgent = false)
                  val regResult = rosmConnector.registerOnce(orgData.ctrNumber, rosmReg)

                  regResult map {
                    case s: RosmRegistrationSuccessResponse => NotImplemented(Json.toJson[RosmRegistrationSuccessResponse](s))
                    case e: RosmRegistrationFailureResponse => InternalServerError(Json.toJson[RosmRegistrationFailureResponse](e))
                  } recover {
                    case _ => InternalServerError(views.html.error.internal_server_error())
                  }
                }
              }
            }
          }
        }
      }

    }
  }

  private def authorisedForLisa(callback: (String) => Future[Result])(implicit request: Request[AnyContent]) = {
    authorised(
      (
        Enrolment("IR-CT") or
        Enrolment("HMCE-VATDEC-ORG") or
        Enrolment("HMCE-VATVAR-ORG")
      ) and
      AuthProviders(GovernmentGateway)
    ).retrieve(internalId) { case internalId =>
      val userId = internalId.getOrElse(throw new RuntimeException("No internalId for logged in user"))

      callback(s"$userId-lisa-registration")
    } recoverWith {
      handleFailure
    }
  }

  private def handleFailure(implicit request: Request[_]) = PartialFunction[Throwable, Future[Result]] {
    case _ : NoActiveSession => Future.successful(toGGLogin("/lifetime-isa/register/organisation-details"))

    case ex:Exception => Future.successful(InternalServerError(ex.getMessage + ex.getStackTrace.mkString))

    // todo: dont assume any controller exception is related to auth - it may be an error in the application code
    case _ => Future.successful(Forbidden(views.html.error.access_denied()))
  }

  private val organisationDetailsCacheKey = "organisationDetails"
  private val tradingDetailsCacheKey = "tradingDetails"
  private val yourDetailsCacheKey = "yourDetails"

  private val organisationForm = Form(
    mapping(
      "companyName" -> nonEmptyText,
      "ctrNumber" -> nonEmptyText
    )(OrganisationDetails.apply)(OrganisationDetails.unapply)
  )

  private val tradingForm = Form(
    mapping(
      "tradingName" -> nonEmptyText,
      "fsrRefNumber" -> nonEmptyText,
      "isaProviderRefNumber" -> nonEmptyText
    )(TradingDetails.apply)(TradingDetails.unapply)
  )

  private val yourForm = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "role" -> nonEmptyText,
      "phone" -> nonEmptyText,
      "email" -> nonEmptyText
    )(YourDetails.apply)(YourDetails.unapply)
  )

}

object Registration extends Registration {
  val authConnector = FrontendAuthConnector
  override val rosmConnector = RosmConnector
  override val cache = ShortLivedCache
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
}