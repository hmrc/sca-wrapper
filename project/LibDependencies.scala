import sbt._

object LibDependencies {
  val bootstrapVersion = "9.0.0"
  private val playHmrcFrontendVersion = "10.3.0"

  val play28 = Seq(
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-28"       % playHmrcFrontendVersion,
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-28"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-language"                    % s"6.2.0-play-28",
    "uk.gov.hmrc"                 %% "play-partials"                    % s"8.4.0-play-28",
    "uk.gov.hmrc"                 %% "domain"                           % s"8.3.0-play-28"
  )

  val play28Test = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-28"         % bootstrapVersion % Test,
    "org.scalatestplus"             %% "mockito-4-6"                    % "3.2.15.0" % Test,
    "com.vladsch.flexmark"           % "flexmark-all"                   % "0.64.8" % Test
  )

  val play29 = Seq(
    "com.typesafe.play"           %% "play-json"                       % "2.10.4",
    "uk.gov.hmrc"                 %% "domain-play-29"                  % "10.0.0",
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-29"      % playHmrcFrontendVersion,
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-29"      % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-language-play-29"           % "7.0.0",
    "uk.gov.hmrc"                 %% "play-partials-play-29"           % "9.1.0"
  )

  val play29Test = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-29"        % bootstrapVersion % Test
  )

  val play30 = Seq(
    "org.playframework"           %% "play-json"                        % "3.0.1",
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-30"       % playHmrcFrontendVersion,
    "uk.gov.hmrc"                 %% "domain-play-30"                  % "10.0.0",
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-language-play-30"           % "7.0.0",
    "uk.gov.hmrc"                 %% "play-partials-play-30"           % "9.1.0"
 )

  val play30Test = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-30"        % bootstrapVersion  % Test
  )
}
