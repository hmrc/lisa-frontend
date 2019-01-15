/*
 * Copyright 2019 HM Revenue & Customs
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

import models._
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsError, Json}
import play.api.test.Helpers._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import uk.gov.hmrc.http.{ HeaderCarrier, HttpPost, HttpResponse }

class RosmConnectorSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite
  with RosmJsonFormats {

  "Register Once endpoint" must {

    "return success" when {
      "rosm returns a success message" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(Json.toJson(rosmSuccessResponse))
          )))

        doRegistrationRequest { response =>
          response.json.validate[RosmRegistrationSuccessResponse].get mustBe rosmSuccessResponse
        }
      }
    }

    "return failure" when {
      "rosm returns a success status but a failure response" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(Json.toJson(rosmFailureResponse)))))

        doRegistrationRequest { response =>
          response.json.validate[DesFailureResponse].get mustBe rosmFailureResponse
        }
      }
      "rosm returns a success status and an unexpected json response" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(Json.parse("{}")))))

        doRegistrationRequest { response =>
          response.body mustBe "{ }"
        }
      }
    }

  }

  "Subscribe Many endpoint" must {
    "return success" when {
      "rosm returns a valid payload with utr" in {
        when(mockHttpPost.POST[LisaSubscription, HttpResponse](any(), any(), any())(any(), any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(Json.toJson(desSubscribeSuccessResponse))
          )))

        doSubscribe { response =>
          response.json.validate[DesSubscriptionSuccessResponse].get mustBe desSubscribeSuccessResponse
        }
      }
    }
  }
  private def doRegistrationRequest(callback: (HttpResponse) => Unit) = {
    val request = RosmRegistration(regime = "LISA", requiresNameMatch = false, isAnAgent = false,
      Organisation(organisationName ="CompName",organisationType="LLP"))
    val response = Await.result(SUT.registerOnce("1234567890", request), Duration.Inf)

    callback(response)
  }

  private def doSubscribe(callback: (HttpResponse) => Unit) = {
    val payload =  LisaSubscription("4567890123","SAFEID0124",
      "FCA1234", "compName", ApplicantDetails("name","lastname","role",ContactDetails("7234545","email@email.com")))
    val response = Await.result(SUT.subscribe("Z1234", payload), Duration.Inf)

    callback(response)
  }

  val mockHttpPost = mock[HttpPost]
  implicit val hc = HeaderCarrier()

  object SUT extends RosmConnector {
    override val httpPost = mockHttpPost
  }

  val rosmIndividual = RosmIndividual(
    firstName = "Test",
    lastName = "User",
    dateOfBirth = Some(new DateTime("1980-01-01"))
  )

  val rosmIndividualNoDob = RosmIndividual(
    firstName = "Test",
    lastName = "User"
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

  val rosmSuccessResponseNoDob = RosmRegistrationSuccessResponse(
    safeId = "XE0001234567890",
    agentReferenceNumber = Some("AARN1234567"),
    isEditable = true,
    isAnAgent = false,
    isAnASAgent = Some(false),
    isAnIndividual = true,
    individual = Some(rosmIndividualNoDob),
    address = rosmAddress,
    contactDetails = rosmContactDetails
  )

  val rosmFailureResponse = DesFailureResponse(
    code = "SERVICE_UNAVAILABLE",
    reason = "Dependent systems are currently not responding."
  )

  val desSubscribeSuccessResponse = DesSubscriptionSuccessResponse("123456")

}