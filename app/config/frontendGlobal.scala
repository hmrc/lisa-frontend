/*
 * Copyright 2021 HM Revenue & Customs
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

package config

import com.google.inject.Inject
import play.api.i18n.MessagesApi
import play.api.libs.json.Reads
import play.api.mvc.Results._
import play.api.mvc.{Request, RequestHeader, Result}
import play.api.{Configuration, Environment}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.crypto.json.JsonDecryptor
import uk.gov.hmrc.crypto.{ApplicationCrypto, CryptoWithKeysFromConfig, Protected}
import uk.gov.hmrc.http.cache.client.{CachingException, SessionCache, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class ErrorHandler @Inject()(val messagesApi: MessagesApi,
                             val configuration: Configuration,
                             implicit val appConfig: AppConfig,
                             errorView: views.html.error_template,
                             notFoundView: views.html.page_not_found_template
                            ) extends FrontendErrorHandler {
  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                    (implicit request: Request[_]): HtmlFormat.Appendable = {
    errorView()
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    notFoundView()
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case play.mvc.Http.Status.FORBIDDEN => Future.successful(Forbidden(internalServerErrorTemplate(Request(request, ""))))
      case _                              => super.onClientError(request, statusCode, message)
    }
  }
}

class LisaSessionCache @Inject()(
  val http: HttpClient,
  val runModeConfiguration: Configuration,
  environment: Environment) extends ServicesConfig(runModeConfiguration: Configuration) with SessionCache {

  override lazy val defaultSource: String = getString("appName")

  override lazy val baseUri: String = baseUrl("cachable.session-cache")

  override lazy val domain: String = getString("microservice.services.cachable.session-cache.domain")
}

class LisaShortLivedHttpCaching @Inject()(
  val http: HttpClient,
  val runModeConfiguration: Configuration,
  environment: Environment) extends ServicesConfig(runModeConfiguration: Configuration) with ShortLivedHttpCaching {

  override lazy val defaultSource: String = getString("appName")

  override lazy val baseUri: String = baseUrl("cachable.short-lived-cache")

  override lazy val domain: String = getString("microservice.services.cachable.short-lived-cache.domain")

}

class LisaShortLivedCache @Inject()(
  val appCrypto: ApplicationCrypto,
  override val shortLiveCache: LisaShortLivedHttpCaching) extends ShortLivedCache {

  override implicit lazy val crypto: CryptoWithKeysFromConfig = appCrypto.JsonCrypto

  override def fetchAndGetEntry[T](cacheId: String, key: String)
                                  (implicit hc: HeaderCarrier, rds: Reads[T], executionContext: ExecutionContext): Future[Option[T]] = {
    try {
      val decryptionFormat = new JsonDecryptor()
      val encrypted: Future[Option[Protected[T]]] =
        shortLiveCache.fetchAndGetEntry(cacheId, key)(hc, decryptionFormat, executionContext)
      encrypted.map(op => convert(op)).recover { case e: UpstreamErrorResponse if e.statusCode == 404 => None }
    } catch {
      case e: SecurityException =>
        throw CachingException(s"Failed to fetch a decrypted entry by cacheId:$cacheId and key:$key", e)
    }
  }
}
