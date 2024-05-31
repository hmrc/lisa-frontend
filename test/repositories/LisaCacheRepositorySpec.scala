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

package repositories

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.{JsNumber, JsObject, JsString}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionId
import uk.gov.hmrc.mongo.cache.{CacheItem, DataKey}
import uk.gov.hmrc.mongo.test.MongoSupport

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class LisaCacheRepositorySpec
  extends PlaySpec
    with MongoSupport
    with MockitoSugar {

  val mockConfiguration: Configuration = mock[Configuration]

  val lisaCacheRepository: LisaCacheRepository = LisaCacheRepository(
    mongoComponent = mongoComponent,
    configuration = mockConfiguration
  )

  "LisaCacheRepository" must {

    "return cached data when getFullCache is called and there is data in the cache" in {
      val nameToAddToCache: String = "Harry Potter"
      val nameDataKey: String = "Name"
      val occupationToAddToCache: String = "Wizard"
      val occupationDataKey: String = "Occupation"
      val ageToAddToCache: Int = 18
      val ageDataKey: String = "Age"

      val sessionId: SessionId = SessionId(UUID.randomUUID().toString)
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest().withSession(("sessionId", sessionId.toString))

      val maybeFullCache: Future[Option[CacheItem]] = for {
        _ <- lisaCacheRepository.putSession[String](DataKey(nameDataKey), nameToAddToCache)
        _ <- lisaCacheRepository.putSession[String](DataKey(occupationDataKey), occupationToAddToCache)
        _ <- lisaCacheRepository.putSession[Int](DataKey(ageDataKey), ageToAddToCache)
        fullCache: Option[CacheItem] <- lisaCacheRepository.getFullCache(fakeRequest)
      } yield fullCache

      val fullCache = Await.result(maybeFullCache, Duration.Inf)

      val cacheData: Option[JsObject] = fullCache.map(_.data)
      val cacheSessionId: Option[String] = fullCache.map(_.id)

      cacheData shouldBe Some(
        JsObject(
          fields = Seq(
            (nameDataKey, JsString(nameToAddToCache)),
            (occupationDataKey, JsString(occupationToAddToCache)),
            (ageDataKey, JsNumber(ageToAddToCache))
          )
        )
      )

      cacheSessionId shouldBe Some(sessionId.toString)
    }

    "return None when there is no data in the cache for the session" in {

      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest().withSession(("sessionId", "not_a_session_id"))

      val fullCache: Option[CacheItem] = Await.result(lisaCacheRepository.getFullCache(fakeRequest), Duration.Inf)

      fullCache shouldBe None
    }
  }
}
