resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

sys.env.get("PLAY_VERSION") match {
  case Some("2.8") =>
    // required since we're cross building for Play 2.8 which isn't compatible with sbt 1.9
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
  case _ =>
    libraryDependencySchemes := libraryDependencySchemes.value // or any empty DslEntry
}

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.15.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.5")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.4.0")
//
//sys.env.get("PLAY_VERSION") match {
//  case Some("2.8") =>
//    addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "1.0.0")
//  case _           =>
//    addSbtPlugin("org.scalastyle" % "scalastyle-sbt-plugin" % "1.0.0" exclude("org.scala-lang.modules", "scala-xml_2.12"))
//}

sys.env.get("PLAY_VERSION") match {
  case Some("2.8") =>
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.20")
  case Some("2.9") =>
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.0")
  case _           =>
    addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.0")
}
