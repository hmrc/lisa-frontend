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
import org.mockito.Matchers._
import org.mockito.Mockito._
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

      "all methods result in a happy path" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(successfulRetrieval)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        val result = SUT.userStatus

        result map { status =>
          status mustBe UserAuthorised("1234", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)
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

        result map { _ =>
          fail("Future succeeded")
        } recover {
          case ex: RuntimeException => ex.getMessage() mustBe "No internalId for logged in user"
          case _ => fail("unexpected error")
        }
      }

      "a userDetailsUri isn't returned from auth" in {
        val invalidRetrievalResult: Future[~[Option[String], Option[String]]] = Future.successful(new ~(Some("1234"), None))

        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(invalidRetrievalResult)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = Some("group"))))

        val result = SUT.userStatus

        result map { _ =>
          fail("Future succeeded")
        } recover {
          case ex: RuntimeException => ex.getMessage() mustBe "No userDetailsUri"
          case _ => fail("unexpected error")
        }
      }

      "the user does not have a groupId" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](any(), any())(any())).
          thenReturn(successfulRetrieval)

        when(mockUserDetailsConnector.getUserDetails(any())(any())).
          thenReturn(Future.successful(UserDetails(None, None, "", groupIdentifier = None)))

        val result = SUT.userStatus

        result map { _ =>
          fail("Future succeeded")
        } recover {
          case ex: RuntimeException => ex.getMessage() mustBe "Could not get groupIdentifier"
          case _ => fail("unexpected error")
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

  when(mockTaxEnrolmentService.getLisaSubscriptionState(any())(any())).thenReturn(Future.successful(TaxEnrolmentDoesNotExist))

}
