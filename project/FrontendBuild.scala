import sbt._

object FrontendBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "lisa-frontend"
  val appVersion = envOrElse("LISA_FRONTEND_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "7.19.0",
    "uk.gov.hmrc" %% "play-partials" % "5.3.0",
    "uk.gov.hmrc" %% "play-auth" % "1.0.0",
    "uk.gov.hmrc" %% "play-config" % "4.2.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.1.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "uk.gov.hmrc" %% "play-ui" % "7.0.0",
    "uk.gov.hmrc" %% "http-caching-client" % "6.2.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
    "org.scalatest" %% "scalatest" % "2.2.6" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.8.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-core" % "1.9.0" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"  % scope
  )

  def apply() = compile ++ test()

}
