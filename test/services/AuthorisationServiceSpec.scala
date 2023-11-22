/*
 * Copyright 2023 HM Revenue & Customs
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

import models._
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.{ExecutionContext, Future}

class AuthorisationServiceSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite
  with ScalaFutures
  with MongoSupport {

  override lazy val fakeApplication: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> "false")
    .overrides(
      bind(classOf[MongoComponent]).toInstance(mongoComponent)
    )
    .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  "user status" should {

    "return an enrolled user" when {

      "an enrolment is in auth" in {
        val enrolmentIdentifier = EnrolmentIdentifier("ZREF", "Z123456")
        val validEnrolment = new Enrolment(key = "HMRC-LISA-ORG", identifiers = List(enrolmentIdentifier), state = "Activated")

        when(mockAuthConnector.authorise[~[~[Option[String], Option[String]], Enrolments]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(buildRetrieval(Some("1234"), Some("/"), Set(validEnrolment)))

        whenReady(SUT.userStatus){
          _ mustBe UserAuthorisedAndEnrolled("1234", "Z123456")
        }
      }

      "a successful enrolment is in tax enrolments" in {
        when(mockAuthConnector.authorise[~[~[Option[String], Option[String]], Enrolments]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(buildRetrieval(Some("1234"), Some("/"), Set()))

        when(mockTaxEnrolmentService.getNewestLisaSubscription(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(validTaxEnrolmentSubscription)))

        whenReady(SUT.userStatus){
          _ mustBe UserAuthorisedAndEnrolled("1234", "Z0001")
        }
      }

    }

    "return an authorised user when no enrolment is in auth and" when {

      "a pending state enrolment is in tax enrolments" in {
        when(mockAuthConnector.authorise[~[~[Option[String], Option[String]], Enrolments]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(buildRetrieval(Some("1234"), Some("/"), Set()))

        when(mockTaxEnrolmentService.getNewestLisaSubscription(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(
            validTaxEnrolmentSubscription.copy(state = TaxEnrolmentPending, identifiers = Nil))))

        whenReady(SUT.userStatus){
          _ mustBe UserAuthorised("1234", TaxEnrolmentPending)
        }
      }

      "an error state enrolment is in tax enrolments" in {
        when(mockAuthConnector.authorise[~[~[Option[String], Option[String]], Enrolments]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(buildRetrieval(Some("1234"), Some("/"), Set()))

        when(mockTaxEnrolmentService.getNewestLisaSubscription(ArgumentMatchers.any())(ArgumentMatchers.any())).
          thenReturn(Future.successful(Some(
            validTaxEnrolmentSubscription.copy(state = TaxEnrolmentError, identifiers = Nil))))

        whenReady(SUT.userStatus){
          _ mustBe UserAuthorised("1234", TaxEnrolmentError)
        }
      }

      "an enrolment is not in tax enrolments" in {
        when(mockAuthConnector.authorise[~[~[Option[String], Option[String]], Enrolments]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(buildRetrieval(Some("1234"), Some("/"), Set()))

        when(mockTaxEnrolmentService.getNewestLisaSubscription(ArgumentMatchers.any())(ArgumentMatchers.any())).
          thenReturn(Future.successful(None))

        whenReady(SUT.userStatus){
          _ mustBe UserAuthorised("1234", TaxEnrolmentDoesNotExist)
        }
      }

    }

    "return user not logged in" when {

      "a NoActiveSession exception is returned from auth" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.failed(BearerTokenExpired()))

        whenReady(SUT.userStatus){
          _ mustBe UserNotLoggedIn
        }
      }

    }

    "return user not admin" when {

      "a UnsupportedCredentialRole exception is returned from auth" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.failed(UnsupportedCredentialRole()))

        whenReady(SUT.userStatus){
          _ mustBe UserNotAdmin
        }
      }

    }

    "return user unauthorised" when {

      "an AuthorisationException exception is returned from auth" in {
        when(mockAuthConnector.authorise[~[Option[String], Option[String]]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.failed(InsufficientEnrolments()))

        whenReady(SUT.userStatus){
          _ mustBe UserUnauthorised
        }
      }

      "internalId retrieval from auth fails" in {
        when(mockAuthConnector.authorise[~[~[Option[String], Option[String]], Enrolments]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(buildRetrieval(None, Some("/"), Set()))

        whenReady(SUT.userStatus){
          _ mustBe UserUnauthorised
        }
      }

      "groupId retrieval from auth fails" in {
        when(mockAuthConnector.authorise[~[~[Option[String], Option[String]], Enrolments]](ArgumentMatchers.any(), ArgumentMatchers.any())
          (ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(buildRetrieval(Some("1234"), None, Set()))

        whenReady(SUT.userStatus){
          _ mustBe UserUnauthorised
        }
      }

    }

  }


  private val mockAuthConnector = mock[AuthConnector]
  private val mockTaxEnrolmentService = mock[TaxEnrolmentService]

  object SUT extends AuthorisationService(mockAuthConnector, mockTaxEnrolmentService)

  private def buildRetrieval(id: Option[String], groupId: Option[String], enrolments: Set[Enrolment]): Future[Option[String] ~ Option[String] ~ Enrolments] = {
    Future.successful(new ~(new ~(id, groupId), Enrolments(enrolments)))
  }

  private val validTaxEnrolmentSubscription = TaxEnrolmentSubscription(
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
