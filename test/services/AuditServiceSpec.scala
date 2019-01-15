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

package services

import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.http.HeaderCarrier

class AuditServiceSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite
  with BeforeAndAfter {

  "AuditService" must {

    before {
      reset(mockAuditConnector)
    }

    "build an audit event with the correct details" in {
      SUT.audit("applicationReceived", "/submit-application", Map("companyName" -> "New Bank", "firstName" -> "John"))

      val captor = ArgumentCaptor.forClass(classOf[DataEvent])

      verify(mockAuditConnector).sendEvent(captor.capture())(any(), any())

      val event = captor.getValue

      event.auditSource mustBe "lisa-frontend"
      event.auditType mustBe "applicationReceived"

      event.tags must contain ("path" -> "/submit-application")
      event.tags must contain ("transactionName" -> "applicationReceived")
      event.tags must contain key "clientIP"

      event.detail must contain ("companyName" -> "New Bank")
      event.detail must contain ("firstName" -> "John")
      event.detail must contain key "Authorization"
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()
  val mockAuditConnector: AuditConnector = mock[AuditConnector]

  object SUT extends AuditService {
    override val connector: AuditConnector = mockAuditConnector
  }
}