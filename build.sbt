organization := "seeder"

name := "sprayapp"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

javaHome := Some(file("/opt/jdk1.8.0_11/"))

fork := true

crossPaths := true

javaOptions := Seq("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
  Seq( base / "src/main/resources", base / "src/main/twirl" )
}

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  val slf4jV =  "1.7.6"
  val slickPgV = "0.6.5.2"
  val scaldiV = "0.4"
  Seq(
    "io.spray"            %   "spray-can_2.11"       % sprayV,
    "io.spray"            %   "spray-routing_2.11"   % sprayV,
    "io.spray"            %   "spray-testkit_2.11"   % sprayV,
    "io.spray"            %   "spray-caching_2.11"   % sprayV,
    "com.typesafe.play"   %%  "twirl-api"            % "1.0.2",
    "io.spray"            %%  "spray-json"      % "1.3.0",
    "net.virtual-void"    %%  "json-lenses"     % "0.6.0",
    "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
    "com.typesafe.akka"   %%  "akka-slf4j"      % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"    % akkaV,
    "ch.qos.logback"      %   "logback-classic" % "1.1.1",
    "org.specs2"          %%  "specs2"          % "2.4.6" % "test",
    "com.typesafe.slick"  %%  "slick"           % "2.1.0",
    "org.mindrot"         %   "jbcrypt"         % "0.3m",
    "org.scalatest"       %   "scalatest_2.11"  % "2.2.1",
    "org.scalamock"       %% "scalamock-scalatest-support" % "3.2",
    "com.h2database"      %   "h2"              % "1.3.175",
    "com.jolbox"          %   "bonecp"          % "0.8.0.RELEASE",
    "org.clapper"         %%  "grizzled-slf4j"  % "1.0.2",
    "nl.grons"            %%  "metrics-scala"   % "3.3.0_a2.3",
    "org.postgresql"      %   "postgresql"      % "9.3-1101-jdbc4",
    "com.github.tminglei" %% "slick-pg" % slickPgV,
    "com.github.tminglei" %% "slick-pg_jts" % slickPgV,
    "com.github.tminglei" %% "slick-pg_date2" % slickPgV,
    "com.github.tminglei" %% "slick-pg_spray-json" % slickPgV,
    "com.google.guava"    % "guava"         % "17.0",
    "com.google.code.findbugs" % "jsr305"    % "3.0.0",
    "org.scaldi"         %% "scaldi"          % scaldiV,
    "org.scaldi"         %% "scaldi-akka"     % scaldiV
  )
}

seq(Revolver.settings: _*)

lazy val sprayapp = (project in file(".")).enablePlugins(SbtTwirl)

seq(bintrayResolverSettings:_*)

seq(flywaySettings: _*)

net.virtualvoid.sbt.graph.Plugin.graphSettings


mainClass in Revolver.reStart := Some("Main")

flywayUrl := "jdbc:postgresql://localhost:5432/app1"

flywayUser := "app"

flywayPassword := "123456"

flywayInitDescription := "<< Flyway Init >>"


