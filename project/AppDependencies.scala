/*
 * Copyright 2023 HM Revenue & Customs
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
  val bootstrapVersion = "7.23.0"
  val hmrcMongoPlayVersion = "1.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"       % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-partials"                    % "8.4.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"               % hmrcMongoPlayVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"               % "7.29.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"         %% "scalatest"                % "3.2.17",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-28"  % hmrcMongoPlayVersion,
    "uk.gov.hmrc"           %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.scalatestplus"     %% "mockito-4-11"             % "3.2.17.0",
    "com.vladsch.flexmark"  % "flexmark-all"              % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
