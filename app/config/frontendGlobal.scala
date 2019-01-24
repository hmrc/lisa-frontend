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

package config

import com.google.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.api.{Configuration, Environment}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.bootstrap.http.{FrontendErrorHandler, HttpClient}
import uk.gov.hmrc.play.config.ServicesConfig

class ErrorHandler @Inject()(val messagesApi: MessagesApi, val configuration: Configuration, implicit val appConfig: AppConfig) extends FrontendErrorHandler {
  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)
                                    (implicit request: Request[_]): HtmlFormat.Appendable = {
    views.html.error_template(pageTitle, heading, message)
  }
}

class LisaSessionCache @Inject()(
  val http: HttpClient,
  val runModeConfiguration: Configuration,
  environment: Environment) extends SessionCache with ServicesConfig {

  override val mode = environment.mode

  override lazy val defaultSource: String = runModeConfiguration.getString("appName").
    getOrElse(throw new Exception("appName not defined"))

  override lazy val baseUri: String = baseUrl("cachable.session-cache")

  override lazy val domain: String = getString("cachable.session-cache.domain")
}

class LisaShortLivedHttpCaching @Inject()(
  val http: HttpClient,
  val runModeConfiguration: Configuration,
  environment: Environment) extends ShortLivedHttpCaching with ServicesConfig {

  override val mode = environment.mode

  override lazy val defaultSource: String = runModeConfiguration.getString("appName").
    getOrElse(throw new Exception("appName not defined"))

  override lazy val baseUri: String = baseUrl("cachable.short-lived-cache")

  override lazy val domain: String = getString("cachable.short-lived-cache.domain")

}

class LisaShortLivedCache @Inject()(
  val appCrypto: ApplicationCrypto,
  override val shortLiveCache: LisaShortLivedHttpCaching) extends ShortLivedCache {

  override implicit lazy val crypto = appCrypto.JsonCrypto
}