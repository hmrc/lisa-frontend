/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val gtmEnabled: Boolean
  val gtmAppId: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val signOutUrl: String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val loginCallback:String
}

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val caFrontendHost = configuration.getString("ca-frontend.host").getOrElse("")
  private val contactHost = configuration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "LISA"
  private val logoutCallback = configuration.getString("gg-urls.logout-callback.url").getOrElse("/lifetime-isa")

  lazy val apiUrl: String = loadConfig("external-urls.lisa-api.url")
  lazy val registerOrgUrl: String = loadConfig("gg-urls.registerOrg.url")

  override lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  override lazy val gtmEnabled: Boolean = configuration.getBoolean(s"google-tag-manager.enabled").getOrElse(false)
  override lazy val gtmAppId: String = loadConfig(s"google-tag-manager.id")
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val signOutUrl = getSignOutUrl(logoutCallback)
  override lazy val betaFeedbackUrl: String = s"$contactHost/contact/beta-feedback"
  override lazy val betaFeedbackUnauthenticatedUrl: String = s"$contactHost/contact/beta-feedback-unauthenticated"
  override lazy val loginCallback: String = configuration.getString("gg-urls.login-callback.url").getOrElse("/lifetime-isa")

  def getSignOutUrl(callbackUrl: String): String = {
    val encodedCallbackUrl = java.net.URLEncoder.encode(callbackUrl, "UTF-8")

    s"$caFrontendHost/gg/sign-out?continue=$encodedCallbackUrl"
  }

}