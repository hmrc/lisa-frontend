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

package helpers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, JsValue}
import repositories.LisaCacheRepository
import uk.gov.hmrc.mongo.cache.CacheItem

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class FullCacheTest(dataComponents: Seq[(String, JsValue)])(implicit val lisaCacheRepository: LisaCacheRepository) {

  def generateMaybeCacheItem(data: Seq[(String, JsValue)]): Option[CacheItem] =
    data match {
      case Nil => None
      case _ => Some(
        CacheItem(
          id = "",
          data = JsObject(data),
          createdAt = LocalDate.parse("2013-11-13").atStartOfDay().toInstant(ZoneOffset.UTC),
          modifiedAt = LocalDate.parse("2013-11-13").atStartOfDay().toInstant(ZoneOffset.UTC)
        )
      )
    }

  when(lisaCacheRepository.getFullCache(any()))
    .thenReturn(
      Future.successful(
        generateMaybeCacheItem(dataComponents)
      )
    )

}
