import sbt._

object LibDependencies {

  private val bootstrapVersion = "8.4.0"
  private val playFrontendHmrVersion = "8.5.0"
  private val domainVersion = "9.0.0"
  private val playLanguageVersion = "7.0.0"
  private val playPartialsVersion = "9.1.0"

  val play28 = Seq(
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-28"       % playFrontendHmrVersion,
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-28"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% "play-language-play-28"            % playLanguageVersion,
    "uk.gov.hmrc"                 %% "play-partials-play-28"            % playPartialsVersion,
    "uk.gov.hmrc"                 %% "domain-play-28"                   % domainVersion
  )

  val play28Test = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-28"         % bootstrapVersion % Test,
    "org.scalatestplus"             %% "mockito-4-6"                    % "3.2.15.0" % Test,
    "com.github.tomakehurst"         %  "wiremock-jre8"                 % "2.35.0" % Test,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2" % Test,
    "com.vladsch.flexmark"            %  "flexmark-all"                 % "0.64.8" % Test
  )

  val play29 = Seq(
    "uk.gov.hmrc"                 %% s"domain-play-29"                  % domainVersion,
    "uk.gov.hmrc"                 %% s"play-frontend-hmrc-play-29"      % playFrontendHmrVersion,
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-29"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% s"play-language-play-29"           % playLanguageVersion,
    "uk.gov.hmrc"                 %% s"play-partials-play-29"           % playPartialsVersion
  )

  val play29Test = Seq(
    "uk.gov.hmrc"                   %% s"bootstrap-test-play-29"        % bootstrapVersion % Test,
    "org.scalatestplus"             %% "mockito-4-6"                    % "3.2.15.0" % Test,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2" % Test
  )

  val play30 = Seq(
    "uk.gov.hmrc"                 %% "play-frontend-hmrc-play-30"       % playFrontendHmrVersion,
    "uk.gov.hmrc"                 %% s"domain-play-30"                  % domainVersion,
    "uk.gov.hmrc"                 %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"                 %% s"play-language-play-30"           % playLanguageVersion,
    "uk.gov.hmrc"                 %% s"play-partials-play-30"           % playPartialsVersion
 )

  val play30Test = Seq(
    "uk.gov.hmrc"                   %% s"bootstrap-test-play-30"        % bootstrapVersion  % Test,
    "org.scalatestplus"             %% "mockito-4-6"                    % "3.2.15.0"  % Test,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2"  % Test
  )
}