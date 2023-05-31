import play.sbt.routes.RoutesKeys

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtTwirl)
  .disablePlugins(PlayLayoutPlugin, JUnitXmlReportPlugin)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(
    scalaVersion := "2.13.8",
    isPublicArtefact := true,
    //TODO tests to check SNAPSHOT is changed back
//    version := "1.0.34",
    version := "1.0.0-SNAPSHOT",
    //    publish / skip := true,
    name := "sca-wrapper",
    isSnapshot := true,
    dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter(organization = "com.vladsch.flexmark"),
    TwirlKeys.templateImports := templateImports,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    libraryDependencies ++= appDependencies ++ testDependencies,
    IntegrationTest / Keys.fork := false,
    inConfig(Test)(testSettings),
    inConfig(IntegrationTest)(itSettings)
  )
  .settings(CodeCoverageSettings.settings: _*)

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
  parallelExecution := true,
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
  "com.typesafe.play"           %% "play-json"                        % "2.9.4",
  "uk.gov.hmrc"                 %% "play-frontend-hmrc"               % s"7.3.0-$playVersion",
  "uk.gov.hmrc"                 %% s"bootstrap-frontend-$playVersion" % "7.13.0",
  "uk.gov.hmrc"                 %% "play-language"                    % s"6.1.0-$playVersion",
  "uk.gov.hmrc"                 %% "play-partials"                    % s"8.4.0-$playVersion",
  "uk.gov.hmrc"                 %% "domain"                           % s"8.1.0-$playVersion",
  "uk.gov.hmrc"                 %% "play-frontend-pta"                % "0.4.0"
)

val testDependencies = Seq(
  "uk.gov.hmrc"                   %% s"bootstrap-test-$playVersion"     % "7.13.0",
  "org.scalatest"                 %% "scalatest"                        % "3.2.15",
  "org.scalatestplus"             %% "mockito-4-6"                      % "3.2.15.0",
  "com.github.tomakehurst"        %  "wiremock-jre8"                    % "2.35.0",
  "com.vladsch.flexmark"          % "flexmark-all"                      % "0.62.2",
  "com.fasterxml.jackson.module"  %% "jackson-module-scala"             % "2.14.2"
).map(_ % "test,it")

