resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  "Flyway" at "http://flywaydb.org/repo"
)

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")


resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.0")


