/*
 * Copyright 2024 HM Revenue & Customs
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

import config.AppConfig
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.LisaCacheRepository
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.ExecutionContext

class AuditServiceSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite
  with BeforeAndAfter
  with MongoSupport {

  override lazy val fakeApplication: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> "false")
    .overrides(
      bind(classOf[MongoComponent]).toInstance(mongoComponent)
    )
    .build()

  implicit val hc:HeaderCarrier = HeaderCarrier()
  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockAppConfig: AppConfig = mock[AppConfig]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val lisaCacheRepository: LisaCacheRepository = mock[LisaCacheRepository]

  object SUT extends AuditService(mockAuditConnector, mockAppConfig)

  "AuditService" must {

    before {
      reset(mockAuditConnector)
      when(mockAppConfig.appName).thenReturn("lisa-frontend")
    }

    "build an audit event with the correct details" in {
      SUT.audit("applicationReceived", "/submit-application", Map("companyName" -> "New Bank", "firstName" -> "John"))

      val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])

      verify(mockAuditConnector).sendEvent(captor.capture())(ArgumentMatchers.any(), ArgumentMatchers.any())

      val event = captor.getValue

      event.auditSource mustBe "lisa-frontend"
      event.auditType mustBe "applicationReceived"

      event.tags must contain ("path" -> "/submit-application")
      event.tags must contain ("transactionName" -> "applicationReceived")
      event.tags must contain key "clientIP"

      event.detail must contain ("companyName" -> "New Bank")
      event.detail must contain ("firstName" -> "John")
    }

  }
}
