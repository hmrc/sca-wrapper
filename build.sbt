ThisBuild / scalaVersion       := "2.13.12"
ThisBuild / majorVersion       := 1
ThisBuild / scalacOptions      += "-Wconf:src=src_managed/.*:s" // silence all warnings on autogenerated files

lazy val sharedSettings: Seq[sbt.Def.SettingsDefinition] = Seq(
  dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang"),
  dependencyUpdatesFilter -= moduleFilter(organization = "com.vladsch.flexmark"),
  scalacOptions += "-Wconf:src=views/.*:s",
  scalacOptions += "-Wconf:src=routes/.*:s",
  TwirlKeys.templateImports := templateImports,
  TwirlKeys.constructorAnnotations += "@javax.inject.Inject()",
  Test / parallelExecution := false,
  Compile / TwirlKeys.compileTemplates / sourceDirectories += baseDirectory.value / s"src/main/twirl",
  Compile / routes / sources ++= {
    // compile any routes files in the root named "routes" or "*.routes"
    val dirs = (Compile / unmanagedResourceDirectories).value
    (dirs * "routes").get ++ (dirs * "*.routes").get
  }
)

def copySources(module: Project) = Seq(
  Compile / scalaSource := (module / Compile / scalaSource).value,
  Compile / resourceDirectory := (module / Compile / resourceDirectory).value,
  Test / scalaSource := (module / Test / scalaSource).value,
  Test / resourceDirectory := (module / Test / resourceDirectory).value
)

def copyPlayResources(module: Project) = Seq(
  Compile / TwirlKeys.compileTemplates / sourceDirectories += (module / baseDirectory).value / s"src/main/twirl",
  Compile / routes / sources ++= {
    // compile any routes files in the root named "routes" or "*.routes"
    val dirs = (module / Compile / unmanagedResourceDirectories).value
    (dirs * "routes").get ++ (dirs * "*.routes").get
  }
)

lazy val root = (project in file("."))
  .settings(name               := "sca-wrapper")
  .settings(CodeCoverageSettings.settings: _*)
  .aggregate(
    sys.env.get("PLAY_VERSION") match {
      case Some("2.8") => play28
      case Some("2.9") => play29
      case _           => play30
    }
  )

lazy val play28 = Project("sca-wrapper-play-28", file("play-28"))
  .disablePlugins(PlayLayoutPlugin, JUnitXmlReportPlugin)
  .enablePlugins(SbtTwirl, RoutesCompiler, BuildInfoPlugin)
  .settings(sharedSettings: _*)
  .settings(
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    libraryDependencies ++= LibDependencies.play28 ++ LibDependencies.play28Test,
    sharedSources
  )

lazy val play29 = Project("sca-wrapper-play-29", file("play-29"))
  .disablePlugins(PlayLayoutPlugin, JUnitXmlReportPlugin)
  .enablePlugins(SbtTwirl, RoutesCompiler, BuildInfoPlugin)
  .settings(copySources(play28))
  .settings(copyPlayResources(play28))
  .settings(sharedSettings: _*)
  .settings(
    libraryDependencies ++= LibDependencies.play29 ++ LibDependencies.play29Test,
    sharedSources
  )

lazy val play30 = Project("sca-wrapper-play-30", file("play-30"))
  .disablePlugins(PlayLayoutPlugin, JUnitXmlReportPlugin)
  .enablePlugins(SbtTwirl, RoutesCompiler, BuildInfoPlugin)
  .settings(sharedSettings: _*)
  .settings(
    libraryDependencies ++= LibDependencies.play30 ++ LibDependencies.play30Test,
    sharedSources
  )

def sharedSources = Seq(
  Compile / unmanagedSourceDirectories   += baseDirectory.value / "../shared/src/main/scala",
  Compile / unmanagedResourceDirectories += baseDirectory.value / "../shared/src/main/resources",
  Test    / unmanagedSourceDirectories   += baseDirectory.value / "../shared/src/test/scala",
  Test    / unmanagedResourceDirectories += baseDirectory.value / "../shared/src/test/resources"
)

lazy val testSettings = Seq(
  unmanagedSourceDirectories ++= Seq(
    baseDirectory.value / "test"
  ),
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
  "views.html.helper._"
)
