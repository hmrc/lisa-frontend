/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.{RosmConnector, RosmJsonFormats}
import models._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.Helpers._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class RosmServiceSpec extends PlaySpec
  with MockitoSugar
  with RosmJsonFormats
  with BeforeAndAfter {

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val mockRosmConnector:RosmConnector = mock[RosmConnector]

  val SUT = new RosmService(mockRosmConnector)

  before {
    reset(mockRosmConnector)
  }

  "RosmService" must {

    "register with ROSM" when {

      "given a valid registration request" in {
        when(mockRosmConnector.registerOnce(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(rosmSuccessResponse), headers = Map.empty)))

        val res = Await.result(SUT.rosmRegister(BusinessStructure("LLP"), org), Duration.Inf)

        res mustBe Right("XE0001234567890")
      }

    }

    "register a business structure of Corporate Body" when {

      "given a business structure of Friendly Society" in {
        when(mockRosmConnector.registerOnce(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(rosmSuccessResponse), headers = Map.empty)))

        val captor = ArgumentCaptor.forClass(classOf[RosmRegistration])

        Await.result(SUT.rosmRegister(BusinessStructure("Friendly Society"), org), Duration.Inf)

        verify(mockRosmConnector).registerOnce(ArgumentMatchers.any(), captor.capture())(ArgumentMatchers.any())

        val submitted = captor.getValue

        submitted.organisation.organisationType mustBe "Corporate Body"
      }

    }

    "return an error from registration" when {

      "it receives a failure response from ROSM" in {
        when(mockRosmConnector.registerOnce(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(rosmFailureResponse), headers = Map.empty)))

        val res = Await.result(SUT.rosmRegister(BusinessStructure("LLP"),org), Duration.Inf)

        res mustBe Left(rosmFailureResponse.code)
      }

      "it receives an invalid response from ROSM" in {
        when(mockRosmConnector.registerOnce(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(""), headers = Map.empty)))

        val res = Await.result(SUT.rosmRegister(BusinessStructure("LLP"),org), Duration.Inf)

        res mustBe Left("INTERNAL_SERVER_ERROR")
      }

    }

    "subscribe with ROSM" when {

      "given a valid submission request" in {
        when(mockRosmConnector.registerOnce(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(rosmSuccessResponse), headers = Map.empty)))
        when(mockRosmConnector.subscribe(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(desSubscribeSuccessResponse), headers = Map.empty)))

        val res = Await.result(SUT.performSubscription(registration), Duration.Inf)

        res mustBe Right("123456")
      }

    }

    "return an error from submission" when {

      "it receives a failure response from ROSM" in {
        when(mockRosmConnector.subscribe(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(rosmFailureResponse), headers = Map.empty)))

        val res = Await.result(SUT.performSubscription(registration), Duration.Inf)

        res mustBe Left(rosmFailureResponse.code)
      }

      "it receives an invalid response from ROSM" in {
        when(mockRosmConnector.subscribe(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(
          Future.successful(HttpResponse(
            status = OK, json = Json.toJson(""), headers = Map.empty)))

        val res = Await.result(SUT.performSubscription(registration), Duration.Inf)

        res mustBe Left("INTERNAL_SERVER_ERROR")
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
    isEditable = true,
    isAnAgent = false,
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

  val org = OrganisationDetails("Test Company Name", "1234567890")

  val registration = LisaRegistration(
    organisationDetails = org,
    tradingDetails = TradingDetails(fsrRefNumber = "123", isaProviderRefNumber = "123"),
    businessStructure = BusinessStructure("LLP"),
    yourDetails = YourDetails(firstName = "Test",lastName = "User",role = "Role", phone = "0191 123 4567",email = "test@test.com"),
    safeId = "56789")
}
