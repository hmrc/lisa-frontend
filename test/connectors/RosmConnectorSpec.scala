/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import helpers.ConnectorSpecHelper
import models.*
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate

class RosmConnectorSpec extends ConnectorSpecHelper with RosmJsonFormats {

  lazy val rosmConnector: RosmConnector = injector.instanceOf[RosmConnector]

  private val registerUrl  = "/lisa/1234567890/register"
  private val subscribeUrl = "/lisa/4567890123/subscribe/Z1234"

  val rosmIndividual = RosmIndividual(
    firstName = "Test",
    lastName = "User",
    dateOfBirth = Some(LocalDate.parse("1980-01-01"))
  )

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
    agentReferenceNumber = Some("AARN1234567"),
    isEditable = true,
    isAnAgent = false,
    isAnASAgent = Some(false),
    isAnIndividual = true,
    individual = Some(rosmIndividual),
    address = rosmAddress,
    contactDetails = rosmContactDetails
  )

  val rosmFailureResponse = DesFailureResponse(
    code = "SERVICE_UNAVAILABLE",
    reason = "Dependent systems are currently not responding."
  )

  val desSubscribeSuccessResponse = DesSubscriptionSuccessResponse("123456")

  "Register Once endpoint" must {

    "return success" when {
      "rosm returns a success message" in {
        stubForPost(registerUrl, CREATED, Json.toJson(rosmSuccessResponse).toString)

        val response = registrationRequest()

        response.status                                             mustBe CREATED
        response.json.validate[RosmRegistrationSuccessResponse].get mustBe rosmSuccessResponse

        server.verify(
          postRequestedFor(urlEqualTo(registerUrl))
            .withRequestBody(equalToJson(Json.toJson(registrationPayload).toString))
        )
      }
    }

    "return failure" when {
      "rosm returns a success status but a failure response" in {
        stubForPost(registerUrl, CREATED, Json.toJson(rosmFailureResponse).toString)

        val response = registrationRequest()

        response.json.validate[DesFailureResponse].get mustBe rosmFailureResponse
      }

      "rosm returns a success status and an unexpected json response" in {
        stubForPost(registerUrl, CREATED, "{}")

        val response = registrationRequest()

        response.json mustBe Json.obj()
      }
    }

    "return the error status and body unchanged" when {
      "rosm returns BAD_REQUEST" in {
        val errorBody =
          Json.toJson(DesFailureResponse("INVALID_PAYLOAD", "Submission has not passed validation.")).toString
        stubForPost(registerUrl, BAD_REQUEST, errorBody)

        val response = registrationRequest()

        response.status mustBe BAD_REQUEST
        response.body   mustBe errorBody
      }

      "rosm returns INTERNAL_SERVER_ERROR" in {
        val errorBody = Json.toJson(rosmFailureResponse).toString
        stubForPost(registerUrl, INTERNAL_SERVER_ERROR, errorBody)

        val response = registrationRequest()

        response.status mustBe INTERNAL_SERVER_ERROR
        response.body   mustBe errorBody
      }
    }
  }

  "Subscribe any endpoint" must {
    "return success" when {
      "rosm returns a valid payload with utr" in {
        stubForPost(subscribeUrl, CREATED, Json.toJson(desSubscribeSuccessResponse).toString)

        val response = subscribeRequest()

        response.status                                            mustBe CREATED
        response.json.validate[DesSubscriptionSuccessResponse].get mustBe desSubscribeSuccessResponse

        server.verify(
          postRequestedFor(urlEqualTo(subscribeUrl))
            .withRequestBody(equalToJson(Json.toJson(subscriptionPayload).toString))
        )
      }
    }
  }

  private val registrationPayload = RosmRegistration(
    regime = "LISA",
    requiresNameMatch = false,
    isAnAgent = false,
    Organisation(organisationName = "CompName", organisationType = "LLP")
  )

  private val subscriptionPayload = LisaSubscription(
    "4567890123",
    "SAFEID0124",
    "FCA1234",
    "compName",
    ApplicantDetails("name", "lastname", "role", ContactDetails("7234545", "email@email.com"))
  )

  private def registrationRequest(): HttpResponse =
    await(rosmConnector.registerOnce("1234567890", registrationPayload))

  private def subscribeRequest(): HttpResponse =
    await(rosmConnector.subscribe("Z1234", subscriptionPayload))

}
