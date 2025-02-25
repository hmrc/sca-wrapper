import sbt._

object LibDependencies {
  val bootstrapVersion = "9.8.0"
  private val playHmrcFrontendVersion = "11.11.0"


  val play29: Seq[ModuleID] = Seq(
    "com.typesafe.play"           %% "play-json"                       % "2.10.6",
    "uk.gov.hmrc"                 %% "domain-play-29"                  % "10.0.0",
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-29"      % playHmrcFrontendVersion,
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-29"      % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-partials-play-29"           % "10.0.0"
  )

  val play29Test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-29"        % bootstrapVersion % Test
  )

  val play30: Seq[ModuleID] = Seq(
    "org.playframework"           %% "play-json"                        % "3.0.4",
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-30"       % playHmrcFrontendVersion,
    "uk.gov.hmrc"                 %% "domain-play-30"                  % "10.0.0",
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-partials-play-30"           % "10.0.0"
 )

  val play30Test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-30"        % bootstrapVersion  % Test
  )
}
