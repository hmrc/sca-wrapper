resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"   % "sbt-auto-build" % "3.24.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage"      % "2.3.1")
addSbtPlugin("uk.gov.hmrc"   % "sbt-distributables" % "2.6.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt"       % "2.5.4")

sys.env.get("PLAY_VERSION") match {
  case Some("2.9") =>
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.7")
  case _           =>
    addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.7")
}
