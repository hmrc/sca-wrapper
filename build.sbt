import play.sbt.routes.RoutesKeys

import scoverage.ScoverageKeys

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin, SbtTwirl)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    scalaVersion := "2.12.16",
    isPublicArtefact := true,
    version := "1.0.0-SNAPSHOT",
    //    publish / skip := true,
    name := "sca-wrapper",
    isSnapshot := true,
    dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter(organization = "com.vladsch.flexmark"),
    ScoverageKeys.coverageMinimumStmtTotal := 20,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    TwirlKeys.templateImports := templateImports,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    libraryDependencies ++= appDependencies ++ testDependencies
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

val appDependencies = Seq(
  "com.typesafe.play" %% "play-json"                % "2.9.3",
  "uk.gov.hmrc" %% "play-frontend-hmrc"       % "3.22.0-play-28",
  "uk.gov.hmrc" %% "bootstrap-frontend-play-28"     % "5.24.0",
  "uk.gov.hmrc" %% "play-language"                  % "5.3.0-play-28",
  "uk.gov.hmrc" %% "play-partials"                % "8.3.0-play-28",
  "uk.gov.hmrc" %% "domain"                         % s"8.0.0-play-28",
  "uk.gov.hmrc"         %% "play-frontend-pta"        % "0.3.0"
)

val testDependencies = Seq(
  "org.scalatest"        %% "scalatest"    % "3.2.7"   % Test,
  "com.vladsch.flexmark" %  "flexmark-all" % "0.35.10" % Test
)
