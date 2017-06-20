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

package services

import connectors.{RosmJsonFormats, RosmConnector}
import models._
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RosmService extends RosmJsonFormats{

  val rosmConnector:RosmConnector

  def registerAndSubscribe(registration: LisaRegistration)(implicit hc:HeaderCarrier): Future[Either[String,String]] =
  {
    val utr = registration.tradingDetails.ctrNumber
    val cName = registration.organisationDetails.companyName

    val rosmRegistration = RosmRegistration("LISA",true,false,Organisation(cName,registration.businessStructure.businessStructure))

    val applicantDetails = ApplicantDetails(registration.yourDetails.firstName,registration.yourDetails.lastName,
      registration.yourDetails.role,ContactDetails(registration.yourDetails.email,registration.yourDetails.phone))

    def performSubscription(safeId:String) : Future[Either[String,String]] =
      rosmConnector.subscribe(registration.tradingDetails.isaProviderRefNumber, LisaSubscription(utr,safeId,registration.tradingDetails.fsrRefNumber, cName, applicantDetails)).map(
        subscribed => subscribed.json.validate[DesSubscriptionSuccessResponse] match {
          case successResponse: JsSuccess[DesSubscriptionSuccessResponse] => Right(successResponse.get.subscriptionId)
          case _ : JsError => handleErrorResponse(subscribed)
        })

    rosmConnector.registerOnce(utr , rosmRegistration).flatMap { res =>
      res.json.validate[RosmRegistrationSuccessResponse] match {
        case successResponse: JsSuccess[RosmRegistrationSuccessResponse] =>  performSubscription(successResponse.get.safeId)
        case _ : JsError => Future(handleErrorResponse(res))
      }
    }
  }

  private def handleErrorResponse(response:HttpResponse)  =  response.json.validate[DesFailureResponse] match {
      case failureResponse: JsSuccess[DesFailureResponse] =>
        Logger.error(s"Des FailureResponse : ${failureResponse.get.code}")
        Left(failureResponse.get.code)
      case _: JsError => Logger.error("JsError in Response")
        Left("INTERNAL_SERVER_ERROR")
    }

}
object RosmService extends RosmService{
 override val rosmConnector: RosmConnector = RosmConnector

}
