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
    "uk.gov.hmrc" %% "frontend-bootstrap" % "7.14.0",
    "uk.gov.hmrc" %% "play-partials" % "5.3.0",
    "uk.gov.hmrc" %% "play-auth" % "0.6.0",
    "uk.gov.hmrc" %% "play-config" % "4.2.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.1.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "uk.gov.hmrc" %% "play-ui" % "7.0.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % "test,it",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test,it",
    "org.pegdown" % "pegdown" % "1.6.0" % "test,it",
    "org.jsoup" % "jsoup" % "1.8.1" % "test,it",
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test,it"
  )

  def apply() = compile ++ test

}
