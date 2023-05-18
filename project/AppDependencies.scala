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
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.15.0",
    "uk.gov.hmrc" %% "play-partials"              % "8.4.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client"        % "10.0.0-play-28",
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.7.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-test-play-28" % "7.15.0",
    "com.typesafe.play"     %% "play-test"              % PlayVersion.current,
    "org.scalatestplus"     %% "mockito-3-4"            % "3.2.10.0",
    "com.vladsch.flexmark"  % "flexmark-all"            % "0.64.4"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
