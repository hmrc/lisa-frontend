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

import play.api.Configuration
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.cache.{CacheItem, SessionCacheRepository}
import uk.gov.hmrc.mongo.{CurrentTimestampSupport, MongoComponent}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class LisaCacheRepository @Inject()(
                                   mongoComponent: MongoComponent,
                                   configuration: Configuration
                                 )(implicit ec: ExecutionContext)
  extends SessionCacheRepository (
    mongoComponent   = mongoComponent,
    collectionName   = "sessions",
    ttl              = Duration(configuration.get[Int]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS),
    timestampSupport = new CurrentTimestampSupport(),
    sessionIdKey = SessionKeys.sessionId
){
    def getFullCache(request: Request[AnyContent]): Future[Option[CacheItem]] =
        cacheRepo.findById(request)
}
