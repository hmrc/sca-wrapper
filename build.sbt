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

import sbt.*
import sbt.Keys.*

val libName = "sca-wrapper"

val scala2_13 = "2.13.16"
val scala3_3 = "3.3.5"

// Disable multiple project tests running at the same time, since notablescan flag is a global setting.
// https://www.scala-sbt.org/1.x/docs/Parallel-Execution.html
Global / concurrentRestrictions += Tags.limitSum(1, Tags.Test, Tags.Untagged)

ThisBuild / scalaVersion := scala2_13
ThisBuild / majorVersion := 3
ThisBuild / isPublicArtefact := true
ThisBuild / organization := "uk.gov.hmrc"
ThisBuild / scalafmtOnCompile := true

lazy val projects: Seq[ProjectReference] = sys.env.get("PLAY_VERSION") match {
  case _ => Seq(play30, play30Test)
}
lazy val library = Project(libName, file("."))
  .settings(publish / skip := true)
  .aggregate(projects *)

def buildScalacOptions(scalaVersion: String): Seq[String] = {
  Seq(
    "-feature",
    "-unchecked",
    "-Wconf:src=routes/.*:s,src=twirl/.*:s",
    "-Wconf:cat=deprecation&msg=method layout in class WrapperService is deprecated:s",
    "-Wconf:cat=deprecation&msg=method safeSignoutUrl in class WrapperService is deprecated:s",
    "-Wconf:cat=deprecation&msg=Please use appConfig for this setting rather than passing it as a parameter:s"
  ) ++ {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, _)) =>
        Seq(
          "-Xlint:_",
          "-Wunused:_",
          "-Wextra-implicit",
          "-Wvalue-discard",
          "-Wdead-code",
          "-Werror",
        )
      case _ =>
        Seq(
          "-Ysafe-init",
          "-language:noAutoTupling",
          "-language:strictEquality",
          "-Wconf:msg=Flag.*repeatedly:s",
          "-Wvalue-discard",
          "-Xfatal-warnings"
        )
    }
  }
}

lazy val play30 = Project(s"$libName-play-30", file(s"$libName-play-30"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(CodeCoverageSettings.settings *)
  .settings(
    TwirlKeys.templateImports := templateImports,
    crossScalaVersions := Seq(scala3_3, scala2_13),
    libraryDependencies ++= LibDependencies.play30 ++ LibDependencies.play30Test,
    Compile / routes / sources ++= {
      val dirs = (Compile / unmanagedResourceDirectories).value
      (dirs * "routes").get ++ (dirs * "*.routes").get
    },
    scalacOptions ++= buildScalacOptions(scalaVersion.value),
    Test / Keys.fork := true,
    Test / parallelExecution := true,
    Test / scalacOptions --= Seq("-language:strictEquality", "-Wdead-code", "-Wvalue-discard")
  )

lazy val play30Test = Project(s"$libName-test-play-30", file(s"$libName-test-play-30"))
  .settings(
    crossScalaVersions := Seq(scala3_3, scala2_13),
    libraryDependencies ++= Seq(
      "uk.gov.hmrc" %% s"bootstrap-test-play-30" % LibDependencies.bootstrapVersion)
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