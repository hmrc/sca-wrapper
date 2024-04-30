/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.Keys.*
import sbt.*

val libName = "sca-wrapper"

val scala2_13 = "2.13.12"

// Disable multiple project tests running at the same time, since notablescan flag is a global setting.
// https://www.scala-sbt.org/1.x/docs/Parallel-Execution.html
Global / concurrentRestrictions += Tags.limitSum(1, Tags.Test, Tags.Untagged)

ThisBuild / scalaVersion       := scala2_13
ThisBuild / majorVersion       := 1
ThisBuild / isPublicArtefact   := true
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
ThisBuild / organization := "uk.gov.hmrc"
ThisBuild / scalafmtOnCompile := true

lazy val projects: Seq[ProjectReference] = sys.env.get("PLAY_VERSION") match {
  case Some("2.8") => Seq(play28, play28Test)
  case Some("2.9") => Seq(play29, play29Test)
  case _ => Seq(play30, play30Test)
}
lazy val library = Project(libName, file("."))
  .settings(publish / skip := true)
  .aggregate(projects: _*)

val buildScalacOptions = Seq(
  "-unchecked",
  "-feature",
  "-Xlint:_",
  "-Wdead-code",
  "-Wunused:_",
  "-Wextra-implicit",
  "-Wvalue-discard",
  "-Werror",
  "-Wconf:cat=unused-imports&site=uk\\.gov\\.hmrc\\.sca\\.views.*:s",
  "-Wconf:cat=unused-imports&site=<empty>:s",
  "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
  "-Wconf:cat=unused&src=.*Routes\\.scala:s",
  "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s",
  "-Wconf:cat=deprecation&site=uk\\.gov\\.hmrc\\.sca\\.views.*:s",
  "-Wconf:cat=deprecation&msg=method apply in class HmrcLayout is deprecated:s",
  "-Wconf:cat=deprecation&msg=method layout in class WrapperService is deprecated:s",
  "-Wconf:cat=deprecation&msg=method safeSignoutUrl in class WrapperService is deprecated:s"

)

def copyPlay30SourcesFor28(module: Project) =
  CopySources.copySources(
    module,
    transformSource   = _.replace("org.apache.pekko", "akka")
      .replace("class Assets @Inject() (errorHandler: HttpErrorHandler, meta: AssetsMetadata, env: Environment)",
        "class Assets @Inject() (errorHandler: HttpErrorHandler, meta: AssetsMetadata)")
      .replace("extends AssetsBuilder(errorHandler, meta, env)",
        "extends AssetsBuilder(errorHandler, meta)")
      .replace("import play.api.Environment",
      "")
    .replace("src/main/resources/messages.en", "target/scala-2.13/resource_managed/main/messages.en")
    .replace("src/main/resources/messages.cy", "target/scala-2.13/resource_managed/main/messages.cy"),
    transformResource = _.replace("pekko", "akka")
  )

def copyPlay30Sources(module: Project) =
  CopySources.copySources(
    module,
    transformSource   = _.replace("org.apache.pekko", "akka")
      .replace("src/main/resources/messages.en", "target/scala-2.13/resource_managed/main/messages.en")
      .replace("src/main/resources/messages.cy", "target/scala-2.13/resource_managed/main/messages.cy"),
    transformResource = _.replace("pekko", "akka")
  )

def copyPlay30Routes(module: Project) = Seq(
  Compile / routes / sources ++= {
    val dirs = (module / Compile / unmanagedResourceDirectories).value
    (dirs * "routes").get ++ (dirs * "*.routes").get
  }
)

lazy val play28 = Project(s"$libName-play-28", file(s"$libName-play-28"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    TwirlKeys.templateImports := templateImports,
    crossScalaVersions := Seq(scala2_13),
    libraryDependencies ++= LibDependencies.play28 ++ LibDependencies.play28Test,
    scalacOptions ++= buildScalacOptions,
    copyPlay30SourcesFor28(play30),
    copyPlay30Routes(play30),
    Test / Keys.fork := true,
    Test / parallelExecution := true,
    Test / scalacOptions --= Seq("-Wdead-code", "-Wvalue-discard")
  )

lazy val play28Test = Project(s"$libName-test-play-28", file(s"$libName-test-play-28"))
  .settings(libraryDependencies ++= Seq(
    "uk.gov.hmrc"         %% s"bootstrap-test-play-28"   % LibDependencies.bootstrapVersion)
  )
  .dependsOn(play28)


lazy val play29 = Project(s"$libName-play-29", file(s"$libName-play-29"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    TwirlKeys.templateImports := templateImports,
    crossScalaVersions := Seq(scala2_13),
    libraryDependencies ++= LibDependencies.play29 ++ LibDependencies.play29Test,
    scalacOptions ++= buildScalacOptions,
    copyPlay30Sources(play30),
    copyPlay30Routes(play30),
    Test / Keys.fork := true,
    Test / parallelExecution := true,
    Test / scalacOptions --= Seq("-Wdead-code", "-Wvalue-discard")
  )

lazy val play29Test = Project(s"$libName-test-play-29", file(s"$libName-test-play-29"))
  .settings(libraryDependencies ++= Seq(
    "uk.gov.hmrc"         %% s"bootstrap-test-play-29"   % LibDependencies.bootstrapVersion)
  )
  .dependsOn(play29)


lazy val play30 = Project(s"$libName-play-30", file(s"$libName-play-30"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    TwirlKeys.templateImports := templateImports,
    crossScalaVersions := Seq(scala2_13),
    libraryDependencies ++= LibDependencies.play30 ++ LibDependencies.play30Test,
    Compile / routes / sources ++= {
      val dirs = (Compile / unmanagedResourceDirectories).value
      (dirs * "routes").get ++ (dirs * "*.routes").get
    },
    scalacOptions ++= buildScalacOptions,
    Test / Keys.fork := true,
    Test / parallelExecution := true,
    Test / scalacOptions --= Seq("-Wdead-code", "-Wvalue-discard")
  )

lazy val play30Test = Project(s"$libName-test-play-30", file(s"$libName-test-play-30"))
  .settings(libraryDependencies ++= Seq(
    "uk.gov.hmrc"         %% s"bootstrap-test-play-30"   % LibDependencies.bootstrapVersion)
  )
  .dependsOn(play30)

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

