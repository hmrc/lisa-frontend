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

import config.{FrontendAuthConnector, LisaShortLivedCache}
import connectors.{RosmConnector, RosmJsonFormats}
import models._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.Future

trait RosmController extends LisaBaseController
  with RosmJsonFormats {

  val rosmConnector:RosmConnector
  val cache:ShortLivedCache

  private val organisationDetailsCacheKey = "organisationDetails"
  private val tradingDetailsCacheKey = "tradingDetails"
  private val yourDetailsCacheKey = "yourDetails"

  private val organisationForm = Form(
    mapping(
      "companyName" -> nonEmptyText,
      "tradingName" -> nonEmptyText
    )(OrganisationDetails.apply)(OrganisationDetails.unapply)
  )

  private val tradingForm = Form(
    mapping(
      "ctrNumber" -> nonEmptyText,
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

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>

      // get organisation details
      cache.fetchAndGetEntry[OrganisationDetails](cacheId, organisationDetailsCacheKey).flatMap {
        case None => Future.successful(Redirect(routes.OrganisationDetailsController.get()))
        case Some(orgData) => {

          // get trading details
          cache.fetchAndGetEntry[TradingDetails](cacheId, tradingDetailsCacheKey).flatMap {
            case None => Future.successful(Redirect(routes.TradingDetailsController.get()))
            case Some(tradData) => {

              // get user details
              cache.fetchAndGetEntry[YourDetails](cacheId, yourDetailsCacheKey).flatMap {
                case None => Future.successful(Redirect(routes.YourDetailsController.get()))
                case Some(yourData) => {
                  val registrationDetails = LisaRegistration(orgData, tradData, yourData)

                  cache.remove(cacheId)

                  val rosmReg = RosmRegistration(regime = "LISA", requiresNameMatch = false, isAnAgent = false)
                  val regResult = rosmConnector.registerOnce(tradData.ctrNumber, rosmReg)

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

}

object RosmController extends RosmController {
  val authConnector = FrontendAuthConnector
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val rosmConnector = RosmConnector
  override val cache = LisaShortLivedCache
}