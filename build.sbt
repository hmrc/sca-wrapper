import play.sbt.routes.RoutesKeys

import scoverage.ScoverageKeys
import play.core.PlayVersion.current

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtTwirl)
  .disablePlugins(PlayLayoutPlugin, JUnitXmlReportPlugin)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(
    scalaVersion := "2.12.15",
    isPublicArtefact := true,
    //TODO tests to check SNAPSHOT is changed back
    version := "1.0.5",
//    version := "1.0.0-SNAPSHOT",
    //    publish / skip := true,
    name := "sca-wrapper",
    isSnapshot := true,
    dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter(organization = "com.vladsch.flexmark"),
    ScoverageKeys.coverageMinimumStmtTotal := 0,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    TwirlKeys.templateImports := templateImports,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    libraryDependencies ++= appDependencies ++ testDependencies,
    test / coverageEnabled := false,
    Test / coverageEnabled := false,
    IntegrationTest / Keys.fork := false,
    inConfig(Test)(testSettings),
    inConfig(IntegrationTest)(itSettings)
  )

lazy val testSettings = Seq(
  unmanagedSourceDirectories ++= Seq(
    baseDirectory.value / "test"
  ),
  fork := true
)

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it"
  ),
  parallelExecution := false,
  fork := true
)

lazy val templateImports: Seq[String] = Seq(
  "_root_.play.twirl.api.Html",
  "_root_.play.twirl.api.HtmlFormat",
  "_root_.play.twirl.api.JavaScript",
  "_root_.play.twirl.api.Txt",
  "_root_.play.twirl.api.Xml",
  "_root_.play.twirl.api.TwirlFeatureImports._",
  "_root_.play.twirl.api.TwirlHelperImports._",
  "play.api.mvc._",
  "play.api.data._",
  "play.api.i18n._",
  "play.api.templates.PlayMagic._",
  "play.twirl.api.HtmlFormat",
  "play.twirl.api.HtmlFormat._",
  "play.twirl.api.TwirlFeatureImports._",
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components.implicits._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
  "uk.gov.hmrc.hmrcfrontend.views.config._",
  "views.html.helper._",
  "uk.gov.hmrc.sca.viewmodels._"
//  "controllers.routes._"
)

val playVersion = "play-28"

val appDependencies = Seq(
  "com.typesafe.play"           %% "play-json"                        % "2.9.3",
  "uk.gov.hmrc"                 %% "play-frontend-hmrc"               % s"3.22.0-$playVersion",
  "uk.gov.hmrc"                 %% s"bootstrap-frontend-$playVersion" % "5.24.0",
  "uk.gov.hmrc"                 %% "play-language"                    % s"5.3.0-$playVersion",
  "uk.gov.hmrc"                 %% "play-partials"                    % s"8.3.0-$playVersion",
  "uk.gov.hmrc"                 %% "domain"                           % s"8.0.0-$playVersion",
  "uk.gov.hmrc"                 %% "play-ui"                          % s"9.8.0-$playVersion",
  "uk.gov.hmrc"                 %% "play-frontend-pta"                % "0.3.0"
)

val testDependencies = Seq(
  "org.scalatest"           %% "scalatest"           % "3.2.8",
  "com.typesafe.play"       %% "play-test"           % current,
  "org.scalatestplus.play"  %% "scalatestplus-play"  % "4.0.3",
  "org.scalatestplus"       %% "mockito-3-4"         % "3.2.3.0",
  "org.mockito"             % "mockito-core"         % "3.6.28",
  "org.scalacheck"          %% "scalacheck"          % "1.15.1",
  "com.github.tomakehurst"  % "wiremock-standalone"  % "2.27.2",
  "com.vladsch.flexmark"    % "flexmark-all"         % "0.36.8"
).map(_ % "test,it")

