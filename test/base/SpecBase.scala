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
import akka.stream.ActorMaterializer
import config.AppConfig
import connectors.EmailConnector
import models.{Reapplication, TaxEnrolmentDoesNotExist, UserAuthorised}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.inject.Injector
import play.api.libs.json.JsValue
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import services.{AuditService, AuthorisationService, RosmService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache, ShortLivedCache}

import scala.concurrent.Future

trait SpecBase extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with BeforeAndAfter {

  val injector: Injector = app.injector
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit val appConfig: AppConfig = injector.instanceOf[AppConfig]
  implicit val env: Environment = injector.instanceOf[Environment]
  implicit val configuration: Configuration = injector.instanceOf[Configuration]
  implicit val shortLivedCache: ShortLivedCache = mock[ShortLivedCache]
  implicit val sessionCache: SessionCache = mock[SessionCache]
  implicit val authorisationService: AuthorisationService = mock[AuthorisationService]
  implicit val rosmService: RosmService = mock[RosmService]
  implicit val auditService: AuditService = mock[AuditService]
  implicit val emailConnector: EmailConnector = mock[EmailConnector]

  before {
    reset(shortLivedCache)
    reset(sessionCache)
    reset(authorisationService)
    reset(rosmService)
    reset(auditService)
    reset(emailConnector)

    when(shortLivedCache.fetchAndGetEntry[Boolean](ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(Reapplication.cacheKey))
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(false)))

    when(shortLivedCache.cache[Any](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(new CacheMap("", Map[String, JsValue]())))

    when(sessionCache.cache(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map[String, JsValue]())))

    when(authorisationService.userStatus(ArgumentMatchers.any()))
      .thenReturn(Future.successful(UserAuthorised("", TaxEnrolmentDoesNotExist)))
  }
}
