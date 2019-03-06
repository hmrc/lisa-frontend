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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

@ImplementedBy(classOf[FrontendAppConfig])
trait AppConfig {
  val appName: String
  val analyticsToken: String
  val analyticsHost: String
  val gtmEnabled: Boolean
  val gtmAppId: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val signOutUrl: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val loginCallback: String
  val apiUrl: String
  val registerOrgUrl: String
  val lisaServiceUrl: String
  val emailServiceUrl: String
  def getSignOutUrl(callbackUrl: String): String
  val displayURBanner: Boolean
}

@Singleton
class FrontendAppConfig @Inject()(
  val runModeConfiguration: Configuration,
  environment: Environment) extends AppConfig with ServicesConfig {

  override val mode = environment.mode

  private def loadConfig(key: String) = getString(key)

  override lazy val lisaServiceUrl = baseUrl("lisa")
  override lazy val emailServiceUrl = baseUrl("email")

  private val caFrontendHost = getString("ca-frontend.host")
  private val contactHost = getString("contact-frontend.host")
  private val contactFormServiceIdentifier = "LISA"
  private val logoutCallback = getString("gg-urls.logout-callback.url")

  override lazy val appName: String = getString("appName")
  override lazy val apiUrl: String = loadConfig("external-urls.lisa-api.url")
  override lazy val registerOrgUrl: String = loadConfig("gg-urls.registerOrg.url")

  override lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  override lazy val gtmEnabled: Boolean = getBoolean(s"google-tag-manager.enabled")
  override lazy val gtmAppId: String = loadConfig(s"google-tag-manager.id")
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val signOutUrl = getSignOutUrl(logoutCallback)
  override lazy val betaFeedbackUrl: String = s"$contactHost/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl: String = s"$contactHost/contact/beta-feedback-unauthenticated"
  override lazy val loginCallback: String = getString("gg-urls.login-callback.url")

  def getSignOutUrl(callbackUrl: String): String = {
    val encodedCallbackUrl = java.net.URLEncoder.encode(callbackUrl, "UTF-8")

    s"$caFrontendHost/gg/sign-out?continue=$encodedCallbackUrl"
  }

  override lazy val displayURBanner: Boolean = getBoolean("display-ur-banner")

}