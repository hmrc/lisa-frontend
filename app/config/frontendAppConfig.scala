/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

@Singleton
class AppConfig @Inject()(runModeConfiguration: Configuration, runMode: RunMode) extends ServicesConfig(runModeConfiguration: Configuration, runMode: RunMode) {

  private def loadConfig(key: String) = getString(key)

  lazy val lisaServiceUrl = baseUrl("lisa")
  lazy val emailServiceUrl = baseUrl("email")

  private val caFrontendHost = getString("ca-frontend.host")
  private val contactHost = getString("contact-frontend.host")
  private val contactFormServiceIdentifier = "LISA"
  private val logoutCallback = getString("gg-urls.logout-callback.url")

  lazy val appName: String = getString("appName")
  lazy val apiUrl: String = loadConfig("external-urls.lisa-api.url")
  lazy val feedbackRedirectUrl: String = loadConfig("external-urls.feedback-frontend.url")
  lazy val registerOrgUrl: String = loadConfig("gg-urls.registerOrg.url")

  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  lazy val gtmEnabled: Boolean = getBoolean(s"google-tag-manager.enabled")
  lazy val gtmAppId: String = loadConfig(s"google-tag-manager.id")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val signOutUrl = getSignOutUrl(logoutCallback)
  lazy val betaFeedbackUrl: String = s"$contactHost/contact/beta-feedback"
  lazy val betaFeedbackUnauthenticatedUrl: String = s"$contactHost/contact/beta-feedback-unauthenticated"
  lazy val loginCallback: String = getString("gg-urls.login-callback.url")

  def getSignOutUrl(callbackUrl: String): String = {
    val encodedCallbackUrl = java.net.URLEncoder.encode(callbackUrl, "UTF-8")

    s"$caFrontendHost/gg/sign-out?continue=$encodedCallbackUrl"
  }

  lazy val displayURBanner: Boolean = getBoolean("display-ur-banner")

}