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

import sbt._

object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "2.3.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.65.0-play-26",
    "uk.gov.hmrc" %% "play-partials" % "7.1.0-play-26",
    "uk.gov.hmrc" %% "auth-client" % "3.3.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "9.2.0-play-26",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.48.0-play-26"
  )

  val test: Seq[ModuleID] = Seq(
    "org.pegdown" % "pegdown" % "1.6.0",
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "org.mockito" % "mockito-core" % "3.8.0",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
