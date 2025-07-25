/*
 * Copyright 2024 HM Revenue & Customs
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

import sbt.*

object AppDependencies {

  val bootstrapVersion = "9.16.0"
  val hmrcMongoPlayVersion = "2.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-partials-play-30"            % "10.1.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % hmrcMongoPlayVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"       % "12.7.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"         %% "scalatest"                % "3.2.19",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30"  % hmrcMongoPlayVersion,
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.scalatestplus"     %% "mockito-5-10"             % "3.2.18.0",
    "com.vladsch.flexmark"  % "flexmark-all"              % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
