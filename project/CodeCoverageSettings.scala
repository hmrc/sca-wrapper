import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 82,
    ScoverageKeys.coverageMinimumBranchTotal := 75,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    ScoverageKeys.coverageExcludedPackages:= "<empty>;uk.gov.hmrc.sca.viewmodels.*;.*Routes.*;.*Module.*"
  )
}
