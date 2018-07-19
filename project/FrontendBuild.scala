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
    "uk.gov.hmrc" %% "frontend-bootstrap" % "8.24.0",
    "uk.gov.hmrc" %% "play-partials" % "6.1.0",
    "uk.gov.hmrc" %% "auth-client" % "2.4.0",
    "uk.gov.hmrc" %% "http-caching-client" % "7.0.0"
  )

  def test(scope: String = "test") = Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-core" % "1.9.0" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"  % scope
  )

  def apply() = compile ++ test()

}
