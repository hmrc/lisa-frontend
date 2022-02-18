/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.Inject
import connectors.{RosmConnector, RosmJsonFormats}
import models._
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class RosmService @Inject()(val rosmConnector: RosmConnector) extends RosmJsonFormats with Logging {

  private def handleErrorResponse(rosmType: String, response:HttpResponse)  =  response.json.validate[DesFailureResponse] match {
    case failureResponse: JsSuccess[DesFailureResponse] => {
      logger.error(s"ROSM $rosmType failure: ${failureResponse.get.code}")
      Left(failureResponse.get.code)
    }
    case _: JsError => {
      logger.error(s"ROSM $rosmType failure, unexpected error.")
      Left("INTERNAL_SERVER_ERROR")
    }
  }

  private def getRosmBusinessStructure(input: BusinessStructure): String = {
    if (input.businessStructure == "Friendly Society")
      "Corporate Body"
    else
      input.businessStructure
  }

  def rosmRegister(businessStructure:BusinessStructure, orgDetails: OrganisationDetails)(implicit hc:HeaderCarrier): Future[Either[String,String]] =
  {
    val rosmRegistration = RosmRegistration("LISA",true,false,Organisation(orgDetails.companyName, getRosmBusinessStructure(businessStructure)))

    rosmConnector.registerOnce(orgDetails.ctrNumber, rosmRegistration).map { res =>
      logger.warn(s"ROSM registration response for ${orgDetails.companyName} (${orgDetails.ctrNumber}): ${res.json.toString()}")

      res.json.validate[RosmRegistrationSuccessResponse] match {
        case successResponse: JsSuccess[RosmRegistrationSuccessResponse] =>  Right(successResponse.get.safeId)
        case _ : JsError => handleErrorResponse("registration", res)
      }
    }.recover {
      case NonFatal(ex: Throwable) => {
        logger.error(s"ROSM registration exception: ${ex.getMessage}")
        Left("INTERNAL_SERVER_ERROR")
      }
    }
  }


  def performSubscription(registration: LisaRegistration)(implicit hc:HeaderCarrier) : Future[Either[String,String]] = {

    val utr = registration.organisationDetails.ctrNumber
    val companyName = registration.organisationDetails.companyName
    val safeId = registration.safeId
    val applicantDetails = ApplicantDetails(
      name = registration.yourDetails.firstName,
      surname = registration.yourDetails.lastName,
      position = registration.yourDetails.role,
      contactDetails = ContactDetails(
        phoneNumber = registration.yourDetails.phone,
        emailAddress = registration.yourDetails.email))

    rosmConnector.subscribe(
      lisaManagerRef = registration.tradingDetails.isaProviderRefNumber,
      lisaSubscribe = LisaSubscription(
        utr = utr,
        safeId = safeId,
        approvalNumber = registration.tradingDetails.fsrRefNumber,
        companyName = companyName,
        applicantDetails = applicantDetails)
    ).map(subscribed => {
        logger.warn(s"ROSM subscription response for $companyName ($utr): ${subscribed.json.toString()}")

        subscribed.json.validate[DesSubscriptionSuccessResponse] match {
          case successResponse: JsSuccess[DesSubscriptionSuccessResponse] => Right(successResponse.get.subscriptionId)
          case _: JsError => handleErrorResponse("submission", subscribed)
        }
    })

  }


}
