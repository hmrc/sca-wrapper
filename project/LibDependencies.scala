import sbt._

object LibDependencies {
  val bootstrapVersion = "10.5.0"
  private val playHmrcFrontendVersion = "12.27.0"
  private val webchatVersion = "1.8.0"


  val play30: Seq[ModuleID] = Seq(
    "org.playframework"           %% "play-json"                        % "3.0.6",
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-30"       % playHmrcFrontendVersion,
    "uk.gov.hmrc"                 %% "domain-play-30"                  % "11.0.0", // pinned to 11.0.0 due to support of scala 1.13 dropped
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-partials-play-30"           % "10.2.0",
    "uk.gov.hmrc" %% "digital-engagement-platform-chat-30" % webchatVersion
 )

  val play30Test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-30"        % bootstrapVersion  % Test
  )
}
