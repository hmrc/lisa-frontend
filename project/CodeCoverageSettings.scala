import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  private val patterns = Seq(
    "<empty>",
    "Reverse.*",
    ".*Routes.*"
  )

  private val settings: Seq[Setting[?]] = {
    Seq(
      ScoverageKeys.coverageExcludedPackages := patterns.mkString("", ";", ""),
      ScoverageKeys.coverageMinimumStmtTotal := 90,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
  }


  def apply(): Seq[Setting[?]] = settings

}
