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

package base

import akka.actor.ActorSystem
import config.AppConfig
import connectors.EmailConnector
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.inject.Injector
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import services.{AuditService, AuthorisationService, RosmService}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import repositories.LisaCacheRepository
import uk.gov.hmrc.mongo.cache.DataKey

import java.util.UUID

trait SpecBase
  extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite
  with MongoSupport
  with BeforeAndAfter {

  override lazy val fakeApplication: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> "false")
    .overrides(
      bind(classOf[MongoComponent]).toInstance(mongoComponent)
    )
    .build()

  val sessionId: SessionId = SessionId(UUID.randomUUID().toString)
  val fakeRequest = FakeRequest().withSession(("sessionId", sessionId.toString))

  val injector: Injector = fakeApplication.injector
  implicit val system: ActorSystem = ActorSystem()
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: AppConfig = injector.instanceOf[AppConfig]
  implicit val env: Environment = injector.instanceOf[Environment]
  implicit val configuration: Configuration = injector.instanceOf[Configuration]
  implicit val authorisationService: AuthorisationService = mock[AuthorisationService]
  implicit val rosmService: RosmService = mock[RosmService]
  implicit val auditService: AuditService = mock[AuditService]
  implicit val emailConnector: EmailConnector = mock[EmailConnector]
  implicit val lisaCacheRepository: LisaCacheRepository = mock[LisaCacheRepository]

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  before {
    reset(lisaCacheRepository)
    reset(authorisationService)
    reset(rosmService)
    reset(auditService)
    reset(emailConnector)

    when(lisaCacheRepository.getFromSession[Boolean](DataKey(any[String]()))(any(), any()))
      .thenReturn(Future.successful(Some(false)))

    when(lisaCacheRepository.putSession(DataKey(any[String]()), any())(any(), any(), any()))
      .thenReturn(Future.successful(("", "")))

    when(authorisationService.userStatus(any()))
      .thenReturn(Future.successful(UserAuthorised("", TaxEnrolmentDoesNotExist)))
  }
}
