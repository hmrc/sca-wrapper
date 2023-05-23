import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
