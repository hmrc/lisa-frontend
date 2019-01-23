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

import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

lazy val lisafrontend = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    name := "lisa-frontend",
    majorVersion := 1,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := StaticRoutesGenerator,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
    resolvers ++= Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.jcenterRepo
    ))

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
    ScoverageKeys.coverageMinimum := 70,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}
