import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageMinimumStmtTotal := 100,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    ScoverageKeys.coverageExcludedFiles := ".*\\/Routes\\/Module;.*" ,
    ScoverageKeys.coverageExcludedPackages:= "<empty>;uk.gov.hmrc.sca.viewmodels.*"
  )
}
