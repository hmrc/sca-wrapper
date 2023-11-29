resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.15.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.5")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0"  exclude("org.scala-lang.modules", "scala-xml_2.12"))
addSbtPlugin("org.playframework" % "sbt-plugin"    % "3.0.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.4.0")

