import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 85,
    ScoverageKeys.coverageMinimumBranchTotal := 70,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    ScoverageKeys.coverageExcludedPackages:= "<empty>;uk.gov.hmrc.sca.viewmodels.*;.*Routes.*;.*Module.*;"
  )
}
