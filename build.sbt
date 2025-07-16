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

import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / majorVersion := 1

lazy val lisafrontend = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(scoverageSettings)
  .settings(scalaSettings)
  .settings(defaultSettings())
  .settings(
    name := "lisa-frontend",
    PlayKeys.playDefaultPort := 8884,
    libraryDependencies ++= AppDependencies(),
    dependencyOverrides += "commons-codec" % "commons-codec" % "1.12",
    retrieveManaged := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    ),
    routesGenerator := InjectedRoutesGenerator,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )

scalacOptions+= "-Wconf:src=routes/.*:s"
scalacOptions+= "-Wconf:cat=unused-imports&src=html/.*:s"


val ScoverageExclusionPatterns = List(
  "<empty>",
  "definition.*",
  "sandbox.*",
  "live.*",
  "prod.*",
  "testOnlyDoNotUseInAppConf.*",
  "config",
  "com.kenshoo.play.metrics",
  "app.Routes",
  "app.RoutesPrefix",
  "uk.gov.hmrc.BuildInfo",
  "controllers.javascript",
  "views.*")

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := ScoverageExclusionPatterns.mkString("",";",""),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

