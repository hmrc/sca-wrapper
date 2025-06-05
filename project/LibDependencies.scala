import sbt._

object LibDependencies {
  val bootstrapVersion = "9.12.0"
  private val playHmrcFrontendVersion = "12.3.0"
  private val webchatVersion = "1.7.0"


  val play30: Seq[ModuleID] = Seq(
    "org.playframework"           %% "play-json"                        % "3.0.4",
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-30"       % playHmrcFrontendVersion,
    "uk.gov.hmrc"                 %% "domain-play-30"                  % "11.0.0",
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-partials-play-30"           % "10.0.0",
    "uk.gov.hmrc" %% "digital-engagement-platform-chat-30" % webchatVersion
 )

  val play30Test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-30"        % bootstrapVersion  % Test
  )
}
