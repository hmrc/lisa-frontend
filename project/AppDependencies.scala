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
    "uk.gov.hmrc" %% "bootstrap-frontend-play-27" % "5.5.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.68.0-play-27",
    "uk.gov.hmrc" %% "play-partials" % "8.1.0-play-27",
    "uk.gov.hmrc" %% "http-caching-client" % "9.5.0-play-27",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.77.0-play-27"
  )

  val test: Seq[ModuleID] = Seq(
    "org.pegdown" % "pegdown" % "1.6.0",
    "com.typesafe.play" %% "play-test" % PlayVersion.current,
    "org.mockito" % "mockito-core" % "3.11.2",
    "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3",
    "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
