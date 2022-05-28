lazy val scala2 = "2.13.8"
lazy val supportedScalaVersions = List(scala2)

ThisBuild / organization  :=  "ai.reactivity"
ThisBuild / version       :=  "0.2-SNAPSHOT"
ThisBuild / scalaVersion  :=  scala2
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation")


lazy val root = project.in(file(".")).
  settings(
    name := "akka-cccp",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.6.19",
      "com.typesafe.akka" %% "akka-stream" % "2.6.19",
      "com.typesafe.akka" %% "akka-http-core" % "10.2.9",
      "org.json4s" %% "json4s-jackson" % "4.0.5",
      "ch.qos.logback" %  "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
    )
  )

