scalaVersion in ThisBuild := "2.13.2"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

lazy val root = project.in(file(".")).
  settings(
    name := "akka-cccp",
    organization := "ai.reactivity",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.5",
      "com.typesafe.akka" %% "akka-stream" % "2.6.5",
      "com.typesafe.akka" %% "akka-http-core" % "10.1.12",
      "org.json4s" %% "json4s-jackson" % "3.6.8",
      "ch.qos.logback" %  "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    )
  )

