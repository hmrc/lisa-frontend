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

package base

import config.AppConfig
import connectors.EmailConnector
import models.*
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import play.api.{Application, Configuration, Environment}
import repositories.LisaCacheRepository
import services.{AuditService, AuthorisationService, RosmService}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.mongo.test.MongoSupport

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait SpecBase extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with MongoSupport with BeforeAndAfter {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> "false")
    .overrides(
      bind(classOf[MongoComponent]).toInstance(mongoComponent)
    )
    .build()

  val sessionId: SessionId                             = SessionId(UUID.randomUUID().toString)
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(("sessionId", sessionId.toString))

  val injector: Injector                           = fakeApplication().injector
  given system: ActorSystem                        = ActorSystem()
  given hc: HeaderCarrier                          = HeaderCarrier()
  given messagesApi: MessagesApi                   = injector.instanceOf[MessagesApi]
  given appConfig: AppConfig                       = injector.instanceOf[AppConfig]
  given env: Environment                           = injector.instanceOf[Environment]
  given configuration: Configuration               = injector.instanceOf[Configuration]
  given authorisationService: AuthorisationService = mock[AuthorisationService]
  given rosmService: RosmService                   = mock[RosmService]
  given auditService: AuditService                 = mock[AuditService]
  given emailConnector: EmailConnector             = mock[EmailConnector]
  given lisaCacheRepository: LisaCacheRepository   = mock[LisaCacheRepository]

  given mcc: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  given ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  before {
    reset(lisaCacheRepository)
    reset(authorisationService)
    reset(rosmService)
    reset(auditService)
    reset(emailConnector)

    when(lisaCacheRepository.getFromSession[Boolean](DataKey(any[String]()))(any(), any()))
      .thenReturn(Future.successful(Some(false)))

    when(lisaCacheRepository.putSession(DataKey(any[String]()), any())(any(), any()))
      .thenReturn(Future.successful(("", "")))

    when(authorisationService.userStatus(using any()))
      .thenReturn(Future.successful(UserAuthorised("", TaxEnrolmentDoesNotExist)))
  }

  def returnMessage(key: String): String = stubMessages(mcc.messagesApi).messages(key)

}
