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

package base

import config.AppConfig
import connectors.EmailConnector
import helpers.CSRFTest
import models.{Reapplication, TaxEnrolmentDoesNotExist, UserAuthorised, UserDetails}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.libs.json.JsValue
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import services.{AuditService, AuthorisationService, RosmService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache, ShortLivedCache}

import scala.concurrent.Future

trait SpecBase extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfter with CSRFTest {

  before {
    reset(shortLivedCache)
    reset(sessionCache)
    reset(authorisationService)
    reset(rosmService)
    reset(auditService)
    reset(emailConnector)

    when(shortLivedCache.fetchAndGetEntry[Boolean](any(), org.mockito.Matchers.eq(Reapplication.cacheKey))(any(), any(), any())).
      thenReturn(Future.successful(Some(false)))
    when(shortLivedCache.cache[Any](any(), any(), any())(any(), any(), any())).
      thenReturn(Future.successful(new CacheMap("", Map[String, JsValue]())))

    when(sessionCache.cache(any(), any())(any(), any(), any())).
      thenReturn(Future.successful(CacheMap("", Map[String, JsValue]())))

    when(authorisationService.userStatus(any())).
      thenReturn(Future.successful(UserAuthorised("", UserDetails(None, None, ""), TaxEnrolmentDoesNotExist)))
  }

  val injector: Injector = app.injector

  val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("", ""))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val appConfig: AppConfig = injector.instanceOf[AppConfig]

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val env: Environment = injector.instanceOf[Environment]

  implicit val configuration: Configuration = injector.instanceOf[Configuration]

  implicit val shortLivedCache: ShortLivedCache = mock[ShortLivedCache]

  implicit val sessionCache: SessionCache = mock[SessionCache]

  implicit val authorisationService: AuthorisationService = mock[AuthorisationService]

  implicit val rosmService: RosmService = mock[RosmService]

  implicit val auditService: AuditService = mock[AuditService]

  implicit val emailConnector: EmailConnector = mock[EmailConnector]

}