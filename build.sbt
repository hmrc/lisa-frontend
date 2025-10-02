ThisBuild / scalaVersion := "2.13.16"
ThisBuild / majorVersion := 1

lazy val microservice = Project("lisa-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(CodeCoverageSettings())
  .settings(
    PlayKeys.playDefaultPort := 8884,
    libraryDependencies ++= AppDependencies(),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    ),
    routesGenerator := InjectedRoutesGenerator,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s", "-feature")
  )
  .settings(CodeCoverageSettings())
