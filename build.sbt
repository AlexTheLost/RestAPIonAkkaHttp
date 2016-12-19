scalacOptions += "-feature"

name := "App"

version := "1.0"

scalaVersion := "2.12.0"

val akkaVersion = "2.4.14"
val akkaHttpVersion = "10.0.0"

libraryDependencies ++= Seq(
  // AKKA:
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  // AKKA-HTTP:
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  // LOGGING:
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  // TESTING:
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.0",
  // HTML PARSING:
  "net.ruippeixotog" %% "scala-scraper" % "1.2.0"
)