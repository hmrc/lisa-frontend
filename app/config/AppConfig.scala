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

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: ServicesConfig){

  private def loadConfig(key: String) = config.getString(key)

  lazy val lisaServiceUrl: String = config.baseUrl("lisa")
  lazy val emailServiceUrl: String = config.baseUrl("email")

  private val basGatewayHost = config.getString("bas-gateway.host")
  private val contactHost = config.getString("contact-frontend.host")
  private val logoutCallback = config.getString("gg-urls.logout-callback.url")

  lazy val appName: String = config.getString("appName")
  lazy val apiUrl: String = loadConfig("external-urls.lisa-api.url")
  lazy val feedbackRedirectUrl: String = loadConfig("external-urls.feedback-frontend.url")
  lazy val registerOrgUrl: String = loadConfig("gg-urls.registerOrg.url")

  lazy val signOutUrl: String = getSignOutUrl(logoutCallback)
  lazy val betaFeedbackUrl: String = s"$contactHost/contact/beta-feedback"
  lazy val betaFeedbackUnauthenticatedUrl: String = s"$contactHost/contact/beta-feedback-unauthenticated"
  lazy val loginCallback: String = config.getString("gg-urls.login-callback.url")
  lazy val loginURL: String = s"$basGatewayHost/bas-gateway/sign-in"
  lazy val displayURBanner: Boolean = config.getBoolean("display-ur-banner")

  def getSignOutUrl(callbackUrl: String): String = {
    val encodedCallbackUrl = java.net.URLEncoder.encode(callbackUrl, "UTF-8")
    s"$basGatewayHost/bas-gateway/sign-out-without-state/?continue=$encodedCallbackUrl"
  }
}
