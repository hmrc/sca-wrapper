import sbt._



object LibDependencies {

  val play28Version = "2.8.20"
  val play29Version = "2.9.0"
  val play30Version = "3.0.0"

  val play28 = Seq(
    "com.typesafe.play"           %% "play-json"                        % "2.9.4",
    "uk.gov.hmrc"                 %% "play-frontend-hmrc"               % s"7.29.0-play-28",
    "uk.gov.hmrc"                 %% s"bootstrap-frontend-play-28"      % "7.23.0",
    "uk.gov.hmrc"                 %% "play-language"                    % s"6.2.0-play-28",
    "uk.gov.hmrc"                 %% "play-partials"                    % s"8.4.0-play-28",
    "uk.gov.hmrc"                 %% "domain"                           % s"8.3.0-play-28",
    "uk.gov.hmrc"                 %% "play-frontend-pta"                % "0.5.0"
  )

  val play28Test = Seq(
    "uk.gov.hmrc"                   %% s"bootstrap-test-play-28"        % "7.23.0",
    "org.scalatestplus"             %% "mockito-4-6"                    % "3.2.15.0",
    "com.github.tomakehurst"         %  "wiremock-jre8"                 % "2.35.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2"
  )

  val play29 = Seq(
    "com.typesafe.play"           %% s"play-json"                       % "2.9.4",
    "uk.gov.hmrc"                 %% s"play-frontend-hmrc-play-29"      % "8.1.0",
    "uk.gov.hmrc"                 %% s"bootstrap-frontend-play-29"      % "8.1.0",
    "uk.gov.hmrc"                 %% s"play-language-play-29"           % "7.0.0",
    "uk.gov.hmrc"                 %% s"play-partials-play-29"           % "9.1.0",
    "uk.gov.hmrc"                 %% "play-frontend-pta"                % "0.5.0"
  )

  val play29Test = Seq(
    "uk.gov.hmrc"                   %% s"bootstrap-test-play-29"        % "8.1.0",
    "org.scalatestplus"             %% "mockito-4-6"                    % "3.2.15.0",
    "com.github.tomakehurst"         %  "wiremock-jre8"                 % "2.35.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2"
  )

  val play30 = Seq(
    "org.playframework"           %% "play-json"                        % "3.0.1",
    "uk.gov.hmrc"                 %% s"play-frontend-hmrc-play-30"      % "8.1.0",
    "uk.gov.hmrc"                 %% s"bootstrap-frontend-play-30"      % "8.1.0",
    "uk.gov.hmrc"                 %% s"play-language-play-30"           % "7.0.0",
    "uk.gov.hmrc"                 %% s"play-partials-play-30"           % "9.1.0"//,
//    "uk.gov.hmrc"                 %% "play-frontend-pta"                % "0.5.0" exclude("org.scala-lang.modules", "scala-xml_2.12")
 )

  val play30Test = Seq(
    "uk.gov.hmrc"                   %% s"bootstrap-test-play-30"        % "8.1.0",
    "org.scalatestplus"             %% "mockito-4-6"                    % "3.2.15.0",
    "com.github.tomakehurst"         %  "wiremock-jre8"                 % "2.35.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.14.2"
  )
}