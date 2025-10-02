import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.2.0"
  private val hmrcMongoPlayVersion = "2.9.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % hmrcMongoPlayVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"       % "12.17.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-30"  % hmrcMongoPlayVersion,
    "uk.gov.hmrc"           %% "bootstrap-test-play-30"   % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
