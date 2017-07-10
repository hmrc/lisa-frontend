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

import connectors.UserDetailsConnector
import models._
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisationServiceSpec extends PlaySpec
  with MockitoSugar {

  implicit val hc:HeaderCarrier = HeaderCarrier()

  "user status" must {

    "return an authorised user" when {

      "a user without a subscription is returned" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(successfulRetrieval)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        val result = SUT.userStatus

        result map { status =>
          status mustBe UserAuthorised("1234", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)
        }
      }

      "a user with a pending subscription is returned" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(successfulRetrieval)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        when(mockTaxEnrolmentService.getNewestLisaSubscription(any())(any())).thenReturn(Future.successful(Some(subscription.copy(state = TaxEnrolmentPending))))

        val result = SUT.userStatus

        result map { status =>
          status mustBe UserAuthorised("1234", UserDetails(None, None, ""), TaxEnrolmentPending)
        }
      }

      "a user with a successful subscription is returned" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(successfulRetrieval)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        when(mockTaxEnrolmentService.getNewestLisaSubscription(any())(any())).thenReturn(Future.successful(Some(subscription)))

        val result = SUT.userStatus

        result map { status =>
          status mustBe UserAuthorisedAndEnrolled("1234", UserDetails(None, None, ""), "Z0001")
        }
      }

    }

    "return user not logged in" when {

      "a NoActiveSession exception is returned from auth" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(Future.failed(new BearerTokenExpired()))

        SUT.userStatus map { status =>
          status mustBe UserNotLoggedIn
        }
      }

    }

    "return user unauthorised" when {

      "a AuthorisationException exception is returned from auth" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(Future.failed(new InsufficientEnrolments()))

        SUT.userStatus map { status =>
          status mustBe UserUnauthorised
        }
      }

    }

    "throw an error" when {

      "an internalId isn't returned from auth" in {
        val invalidRetrievalResult: Future[~[Option[String], Option[String]]] = Future.successful(new ~(None, Some("/")))

        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(invalidRetrievalResult)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        val result = SUT.userStatus

        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[RuntimeException]
          e.getMessage mustBe "No internalId for user"
        }
      }

      "a userDetailsUri isn't returned from auth" in {
        val invalidRetrievalResult: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), None))

        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(invalidRetrievalResult)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        val result = SUT.userStatus

        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[RuntimeException]
          e.getMessage mustBe "No userDetailsUri for user"
        }
      }

      "the user does not have a groupId" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(successfulRetrieval)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = None)))

        val result = SUT.userStatus

        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[RuntimeException]
          e.getMessage mustBe "No groupIdentifier for user"
        }
      }

      "the user has a hmrc subscription with a success state and no zref" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(successfulRetrieval)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        when(mockTaxEnrolmentService.getNewestLisaSubscription(any())(any())).thenReturn(Future.successful(Some(subscription.copy(identifiers = Nil))))

        val result = SUT.userStatus

        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[RuntimeException]
          e.getMessage mustBe "No zref for successful enrolment"
        }
      }

    }

  }

  val mockAuthConnector = mock[AuthConnector]
  val mockUserDetailsConnector = mock[UserDetailsConnector]
  val mockTaxEnrolmentService = mock[TaxEnrolmentService]

  object SUT extends AuthorisationService {
    val authConnector: AuthConnector = mockAuthConnector
    override val userDetailsConnector: UserDetailsConnector = mockUserDetailsConnector
    override val taxEnrolmentService: TaxEnrolmentService = mockTaxEnrolmentService
  }

  val successfulRetrieval: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), Some("/")))

  when(mockTaxEnrolmentService.getNewestLisaSubscription(any())(any())).thenReturn(Future.successful(None))

  private val subscription = TaxEnrolmentSubscription(
    created = new DateTime(),
    lastModified = new DateTime(),
    credId = "",
    serviceName = "HMRC-LISA-ORG",
    identifiers = List(TaxEnrolmentIdentifier("ZREF", "Z0001")),
    callback = "",
    state = TaxEnrolmentSuccess,
    etmpId = "",
    groupIdentifier = ""
  )

}
