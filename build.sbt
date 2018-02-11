name := "weather-watcher"

version := "0.1"

scalaVersion := "2.12.4"

// https://mvnrepository.com/artifact/com.typesafe.play/play-json
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.8",
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
)
