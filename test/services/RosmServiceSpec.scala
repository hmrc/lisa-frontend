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
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RosmServiceSpec extends PlaySpec with MockitoSugar with OneAppPerSuite with RosmJsonFormats {

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val mockRosmConnector:RosmConnector = mock[RosmConnector]

  val SUT = new RosmService {
    override val rosmConnector:RosmConnector  = mockRosmConnector
  }

  "RosmService" must {
    "Register" when {
      " a Valid request for submission" in {
        when(mockRosmConnector.registerOnce(any(),any())(any())).thenReturn(
            Future.successful(HttpResponse(OK,Some(Json.toJson(rosmSuccessResponse)))))

        val res = Await.result(SUT.rosmRegister(BusinessStructure("LLP"),org), Duration.Inf)

        res mustBe Right("XE0001234567890")
      }
      "return a Des Error" when {
        "an in-valid request for registration is recieved" in {
          when(mockRosmConnector.registerOnce(any(), any())(any())).thenReturn(
            Future.successful(HttpResponse(OK, Some(Json.toJson(rosmFailureResponse)))))

          val res = Await.result(SUT.rosmRegister(BusinessStructure("LLP"),org), Duration.Inf)

          res mustBe Left(rosmFailureResponse.code)
        }
      }

      "return a Internal Server Error" when {

        "response Json validation has failed in Registration" in {
          when(mockRosmConnector.registerOnce(any(), any())(any())).thenReturn(
            Future.successful(HttpResponse(OK, Some(Json.toJson("")))))

          val res = Await.result(SUT.rosmRegister(BusinessStructure("LLP"),org), Duration.Inf)

          res mustBe Left("INTERNAL_SERVER_ERROR")
        }
      }
    }

    "perform Subscription" when {
      " a Valid request for submission" in {
        when(mockRosmConnector.registerOnce(any(), any())(any())).thenReturn(
          Future.successful(HttpResponse(OK, Some(Json.toJson(rosmSuccessResponse)))))
        when(mockRosmConnector.subscribe(any(), any())(any())).thenReturn(
          Future.successful(HttpResponse(OK, Some(Json.toJson(desSubscribeSuccessResponse)))))

        val res = Await.result(SUT.performSubscription(registration), Duration.Inf)

        res mustBe Right("123456")
      }
      "return a Des Error" when {
        "an in-valid request for submission is recieved" in {
          when(mockRosmConnector.subscribe(any(), any())(any())).thenReturn(
            Future.successful(HttpResponse(OK, Some(Json.toJson(rosmFailureResponse)))))

          val res = Await.result(SUT.performSubscription(registration), Duration.Inf)

          res mustBe Left(rosmFailureResponse.code)
        }
      }
      "return a Internal Server Error" when {
        "response Json validation has failed in submission" in {
          when(mockRosmConnector.subscribe(any(), any())(any())).thenReturn(
            Future.successful(HttpResponse(OK, Some(Json.toJson("")))))

          val res = Await.result(SUT.performSubscription(registration), Duration.Inf)

          res mustBe Left("INTERNAL_SERVER_ERROR")
        }
      }
    }
  }

  val rosmAddress = RosmAddress(
    addressLine1 = "Address Line 1",
    postalCode = Some("AB1 1AB"),
    countryCode = "GB"
  )

  val rosmContactDetails = RosmContactDetails(
    primaryPhoneNumber = Some("0123 456 7890"),
    emailAddress = Some("test@test.com")
  )

  val rosmSuccessResponse = RosmRegistrationSuccessResponse(
    safeId = "XE0001234567890",
    agentReferenceNumber = "AARN1234567",
    isEditable = true,
    isAnAgent = false,
    isAnASAgent = false,
    isAnIndividual = true,
    individual = None,
    address = rosmAddress,
    contactDetails = rosmContactDetails
  )

  val rosmFailureResponse = DesFailureResponse(
    code = "SERVICE_UNAVAILABLE",
    reason = "Dependent systems are currently not responding."
  )

  val desSubscribeSuccessResponse = DesSubscriptionSuccessResponse("123456")

  val org = OrganisationDetails("Test Company Name", "1234567890", Some("56789"))
  val registration = LisaRegistration(OrganisationDetails("Test Company Name", "1234567890", Some("56789")),
        TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123"),
        BusinessStructure("LLP"),YourDetails(firstName = "Test",lastName = "User",role = "Role",
        phone = "0191 123 4567",email = "test@test.com"))
}
